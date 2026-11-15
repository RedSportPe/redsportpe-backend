package com.redsport.backend.orders.domain.model.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(
    name = "saved_cart_lines",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sku"}))
@Getter
public class SavedCartLine {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 60)
  private String sku;

  @Column(nullable = false)
  private int quantity;

  protected SavedCartLine() {}

  public SavedCartLine(UUID userId, String sku, int quantity) {
    this.userId = userId;
    this.sku = sku;
    this.quantity = quantity;
  }
}
