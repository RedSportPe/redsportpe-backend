package com.redsport.backend.inventory.application.internal;

import com.redsport.backend.inventory.application.ports.Inventory;
import com.redsport.backend.inventory.domain.model.aggregates.StockItem;
import com.redsport.backend.inventory.infrastructure.persistence.jpa.repositories.StockRepository;
import com.redsport.backend.shared.domain.model.DomainException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService implements Inventory {
  private final StockRepository stocks;

  public InventoryService(StockRepository stocks) {
    this.stocks = stocks;
  }

  @Transactional(readOnly = true)
  public int available(String sku) {
    return stocks.findById(sku).map(StockItem::available).orElse(0);
  }

  public void initialize(String sku, int quantity) {
    if (stocks.existsById(sku)) throw DomainException.conflict("El SKU ya existe");
    stocks.save(new StockItem(sku, quantity));
  }

  public void reserve(Map<String, Integer> q) {
    each(q, (s, n) -> s.reserve(n));
  }

  public void release(Map<String, Integer> q) {
    each(q, (s, n) -> s.release(n));
  }

  public void commit(Map<String, Integer> q) {
    each(q, (s, n) -> s.commit(n));
  }

  private void each(
      Map<String, Integer> q, java.util.function.BiConsumer<StockItem, Integer> action) {
    q.keySet().stream()
        .sorted()
        .forEach(
            sku ->
                action.accept(
                    stocks
                        .lock(sku)
                        .orElseThrow(
                            () -> DomainException.conflict("No hay inventario para " + sku)),
                    q.get(sku)));
  }

  public List<StockResource> list() {
    return stocks.findAll().stream()
        .map(
            s ->
                new StockResource(
                    s.getSku(), s.getOnHand(), s.getReserved(), s.available(), s.getVersion()))
        .toList();
  }

  public List<StockResource> adjust(List<Adjustment> changes) {
    if (changes.stream().map(Adjustment::sku).distinct().count() != changes.size())
      throw new IllegalArgumentException("SKU repetido");
    changes.stream()
        .sorted(Comparator.comparing(Adjustment::sku))
        .forEach(
            c -> {
              var s =
                  stocks
                      .lock(c.sku())
                      .orElseThrow(() -> DomainException.missing("SKU no encontrado"));
              if (s.getVersion() != c.version())
                throw DomainException.conflict("El stock cambió. Recarga el inventario");
              s.adjust(c.onHand());
            });
    stocks.flush();
    return list();
  }

  public record StockResource(String sku, int onHand, int reserved, int available, long version) {}

  public record Adjustment(
      @jakarta.validation.constraints.NotBlank String sku,
      @jakarta.validation.constraints.Min(0) int onHand,
      @jakarta.validation.constraints.Min(0) long version) {}
}
