package com.redsport.backend.catalog.domain.model.entities;

import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "variants")
@Getter
public class Variant {

  @EmbeddedId private Sku sku;

  @Column(name = "product_id", insertable = false, updatable = false)
  private java.util.UUID productId;

  @Column(name = "gender", length = 2)
  private String gender;

  @Column(name = "size", length = 4)
  private String size;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "color", length = 3)
  private String color;

  protected Variant() {}

  public Variant(Sku sku, String gender, String size, String color) {
    this.sku = sku;
    this.gender = gender;
    this.size = size;
    this.color = color;
  }
}
