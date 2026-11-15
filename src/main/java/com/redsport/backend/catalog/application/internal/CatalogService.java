package com.redsport.backend.catalog.application.internal;

import com.redsport.backend.catalog.application.commands.SaveProductCommand;
import com.redsport.backend.catalog.application.ports.*;
import com.redsport.backend.catalog.application.queries.*;
import com.redsport.backend.catalog.domain.model.aggregates.Product;
import com.redsport.backend.catalog.domain.model.entities.Variant;
import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.redsport.backend.inventory.application.ports.Inventory;
import com.redsport.backend.shared.domain.model.DomainException;
import com.redsport.backend.stores.application.ports.StoreAccess;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogService implements Catalog {
  private final ProductRepository products;
  private final Inventory inventory;
  private final StoreAccess stores;
  private final ProductUsage usage;

  public CatalogService(
      ProductRepository products, Inventory inventory, StoreAccess stores, ProductUsage usage) {
    this.products = products;
    this.inventory = inventory;
    this.stores = stores;
    this.usage = usage;
  }

  @Transactional(readOnly = true)
  public List<ProductView> list(boolean admin) {
    return (admin ? products.findByDeletedFalse() : products.findByPublishedTrueAndDeletedFalse())
        .stream().map(this::resource).toList();
  }

  @Transactional(readOnly = true)
  public ProductView get(UUID id, boolean admin) {
    var p = find(id);
    if (!admin && !p.isPublished()) throw DomainException.missing("Producto no encontrado");
    return resource(p);
  }

  @Transactional(readOnly = true)
  public ProductView lookup(String rawSku) {
    String sku = new Sku(rawSku).value();
    var p = products.findBySku(sku).orElseThrow(() -> DomainException.missing("SKU no encontrado"));
    if (p.isDeleted() || !p.isPublished()) throw DomainException.missing("SKU no encontrado");
    return resource(p);
  }

  private Product find(UUID id) {
    return products
        .findById(id)
        .filter(p -> !p.isDeleted())
        .orElseThrow(() -> DomainException.missing("Producto no encontrado"));
  }

  public ProductView save(UUID id, SaveProductCommand r) {
    var p =
        id == null
            ? new Product(
                r.productCode(),
                r.name(),
                r.description(),
                r.category(),
                r.price(),
                r.published(),
                r.featured())
            : products
                .lockById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> DomainException.missing("Producto no encontrado"));
    if (!p.getProductCode().equals(r.productCode()))
      throw new IllegalArgumentException("No se puede cambiar el código de un producto existente");
    p.update(r.name(), r.description(), r.category(), r.price(), r.published(), r.featured());
    Set<String> seen = new HashSet<>();
    Set<String> old =
        p.getVariants().stream()
            .map(v -> v.getSku().value())
            .collect(java.util.stream.Collectors.toSet());
    for (var v : r.variants()) {
      var sku = new Sku(v.sku());
      String[] parts = sku.value().split("-");
      if (!seen.add(sku.value())
          || !parts[1].equals(r.productCode())
          || !parts[2].equals(v.gender())
          || !parts[3].equals(v.size())
          || !parts[4].equals(v.color()))
        throw new IllegalArgumentException("Variante inconsistente o repetida");
      stores.requireActive(sku.storeCode());
      if (!old.contains(sku.value())) {
        p.addVariant(new Variant(sku, v.gender(), v.size(), v.color()));
        inventory.initialize(sku.value(), v.totalStock());
      }
    }
    if (!seen.containsAll(old))
      throw DomainException.conflict(
          "Los SKU existentes se conservan por trazabilidad; ajusta su stock a cero");
    r.imageUrls()
        .forEach(
            url -> {
              if (!(url.startsWith("https://") || url.startsWith("/api/media/")))
                throw new IllegalArgumentException("URL de imagen inválida");
            });
    p.replaceImages(r.imageUrls());
    products.saveAndFlush(p);
    return resource(p);
  }

  public void publish(UUID id, boolean published) {
    var p =
        products.lockById(id).orElseThrow(() -> DomainException.missing("Producto no encontrado"));
    p.publish(published);
  }

  public void delete(UUID id) {
    var p =
        products.lockById(id).orElseThrow(() -> DomainException.missing("Producto no encontrado"));
    if (usage.hasActiveOrders(id))
      throw DomainException.conflict("El producto tiene pedidos activos");
    p.archive();
  }

  public List<Item> lockItems(Collection<String> rawSkus) {
    var skus = rawSkus.stream().map(s -> new Sku(s).value()).distinct().sorted().toList();
    var ids =
        skus.stream()
            .map(
                s ->
                    products
                        .findProductIdBySku(s)
                        .orElseThrow(() -> DomainException.missing("SKU no encontrado: " + s)))
            .distinct()
            .sorted()
            .toList();
    Map<UUID, Product> locked = new HashMap<>();
    ids.forEach(id -> locked.put(id, products.lockById(id).orElseThrow()));
    return skus.stream()
        .map(
            s -> {
              var p =
                  locked.values().stream()
                      .filter(
                          x -> x.getVariants().stream().anyMatch(v -> v.getSku().value().equals(s)))
                      .findFirst()
                      .orElseThrow();
              if (p.isDeleted() || !p.isPublished())
                throw DomainException.conflict("Producto no disponible");
              stores.requireActive(new Sku(s).storeCode());
              return new Item(s, p.getId(), p.getName(), p.getPrice(), p.coverImageUrl());
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public void requirePublished(UUID id) {
    get(id, false);
  }

  @Transactional(readOnly = true)
  public Item readItem(String rawSku) {
    var sku = new Sku(rawSku);
    sku.storeCode();
    var p =
        products
            .findBySku(sku.value())
            .filter(x -> !x.isDeleted() && x.isPublished())
            .orElseThrow(() -> DomainException.missing("SKU no encontrado"));
    return new Item(sku.value(), p.getId(), p.getName(), p.getPrice(), p.coverImageUrl());
  }

  public BigDecimal lockPrice(UUID id) {
    return products
        .lockById(id)
        .filter(p -> !p.isDeleted())
        .orElseThrow(() -> DomainException.missing("Producto no encontrado"))
        .getPrice();
  }

  public void recordSale(UUID id, int quantity) {
    products.lockById(id).orElseThrow().recordSale(quantity);
  }

  private ProductView resource(Product p) {
    var variants =
        p.getVariants().stream()
            .filter(v -> v.getSku().value().split("-").length == 6)
            .map(
                v ->
                    new VariantView(
                        v.getSku().value(),
                        v.getGender(),
                        v.getSize(),
                        v.getColor(),
                        inventory.available(v.getSku().value())))
            .toList();
    return new ProductView(
        p.getId().toString(),
        p.getName(),
        p.getDescription(),
        p.getCategory(),
        p.getPrice(),
        p.coverImageUrl(),
        p.imageUrls(),
        p.isPublished(),
        p.isFeatured(),
        p.getSalesCount(),
        p.getCreatedAt().toString(),
        variants);
  }
}
