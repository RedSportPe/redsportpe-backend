package com.redsport.backend.orders.domain.model.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "order_items")
@Getter
public class OrderLine {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID productId;

  @Column(name = "variant_sku", nullable = false, length = 60)
  private String sku;

  @Column(name = "name_snapshot", nullable = false, length = 150)
  private String productName;

  @Column(name = "image_snapshot", length = 500)
  private String imageUrl;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal catalogPrice;

  private UUID promotionId;
  @Transient private boolean negotiated;

  public void markNegotiated() {
    negotiated = true;
  }

  protected OrderLine() {}

  public OrderLine(
      UUID productId,
      String sku,
      String name,
      String imageUrl,
      int quantity,
      BigDecimal price,
      BigDecimal catalogPrice,
      UUID promotionId) {
    if (quantity <= 0 || quantity > 9999) throw new IllegalArgumentException("Cantidad inválida");
    this.productId = productId;
    this.sku = sku;
    this.productName = name;
    this.imageUrl = imageUrl;
    this.quantity = quantity;
    this.unitPrice = com.redsport.backend.shared.domain.model.Money.amount(price);
    this.catalogPrice = catalogPrice;
    this.promotionId = promotionId;
  }

  public BigDecimal total() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
