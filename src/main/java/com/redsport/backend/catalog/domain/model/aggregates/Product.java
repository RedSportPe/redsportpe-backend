package com.redsport.backend.catalog.domain.model.aggregates;

import com.redsport.backend.catalog.domain.model.entities.ProductImage;
import com.redsport.backend.catalog.domain.model.entities.Variant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "products")
@Getter
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_code", length = 10, nullable = false)
  private String productCode;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "category", length = 60)
  private String category;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Column(name = "published")
  private boolean published;

  @Column(name = "featured")
  private boolean featured;

  @Column(name = "sales_count")
  private Integer salesCount;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "product_id")
  private List<Variant> variants;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "product_id")
  @OrderBy("position ASC")
  private List<ProductImage> images;

  @Column(nullable = false)
  private boolean deleted;

  @Version private long version;

  protected Product() {}

  public void update(
      String name,
      String description,
      String category,
      BigDecimal price,
      boolean published,
      boolean featured) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre requerido");
    this.name = name.trim();
    this.description = description;
    this.category = category;
    this.price = com.redsport.backend.shared.domain.model.Money.positive(price);
    this.published = published;
    this.featured = featured;
  }

  public void replaceImages(List<String> urls) {
    images.clear();
    for (int i = 0; i < urls.size(); i++) images.add(new ProductImage(urls.get(i), (short) i));
  }

  public void publish(boolean value) {
    if (deleted) throw new IllegalArgumentException("Producto eliminado");
    published = value;
  }

  public void archive() {
    deleted = true;
    published = false;
  }

  public void recordSale(int quantity) {
    salesCount = Math.addExact(salesCount, quantity);
  }

  /** Factory: create a new product (variants and images added after) */
  public Product(
      String productCode,
      String name,
      String description,
      String category,
      BigDecimal price,
      boolean published,
      boolean featured) {
    this.productCode = productCode;
    this.name = name;
    this.description = description;
    this.category = category;
    this.price = com.redsport.backend.shared.domain.model.Money.positive(price);
    this.published = published;
    this.featured = featured;
    this.salesCount = 0;
    this.createdAt = OffsetDateTime.now();
    this.variants = new ArrayList<>();
    this.images = new ArrayList<>();
  }

  public void addVariant(Variant variant) {
    if (this.variants == null) this.variants = new ArrayList<>();
    this.variants.add(variant);
  }

  public void addImage(ProductImage image) {
    if (this.images == null) this.images = new ArrayList<>();
    this.images.add(image);
  }

  public boolean isVisibleInTrends() {
    return published && featured;
  }

  public String coverImageUrl() {
    if (images == null || images.isEmpty()) return null;
    return images.get(0).getUrl();
  }

  public List<String> imageUrls() {
    if (images == null) return List.of();
    return images.stream().map(ProductImage::getUrl).toList();
  }

  public List<Variant> getVariants() {
    return variants == null ? List.of() : Collections.unmodifiableList(variants);
  }
}
