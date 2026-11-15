package com.redsport.backend.catalog.domain.model.aggregates;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
public class Favorite {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID productId;

  protected Favorite() {}

  public Favorite(UUID user, UUID product) {
    userId = user;
    productId = product;
  }
}
