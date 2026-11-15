package com.redsport.backend.catalog.domain.model.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "product_images")
@Getter
public class ProductImage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_id", insertable = false, updatable = false)
  private UUID productId;

  @Column(name = "url", length = 500, nullable = false)
  private String url;

  @Column(name = "position")
  private Short position;

  protected ProductImage() {}

  public ProductImage(String url, Short position) {
    this.url = url;
    this.position = position;
  }
}
