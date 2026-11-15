package com.redsport.backend.promotions.domain.model.aggregates;

import com.redsport.backend.shared.domain.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "promotions")
@Getter
public class Promotion {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID productId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal discountAmount;

  private LocalDate endsAt;
  private Integer maxUnits;

  @Column(nullable = false)
  private int unitsSold;

  @Column(nullable = false)
  private int reservedUnits;

  @Column(nullable = false)
  private boolean cancelled;

  @Column(nullable = false)
  private boolean active = true;

  @Version private long version;

  protected Promotion() {}

  public Promotion(UUID productId, BigDecimal discountAmount, LocalDate endsAt, Integer maxUnits) {
    this.productId = productId;
    this.discountAmount = Money.positive(discountAmount);
    this.endsAt = endsAt;
    this.maxUnits = maxUnits;
    if (maxUnits != null && maxUnits <= 0)
      throw new IllegalArgumentException("El límite de unidades debe ser positivo");
  }

  public boolean active(LocalDate today) {
    return active
        && !cancelled
        && (endsAt == null || !today.isAfter(endsAt))
        && (maxUnits == null || unitsSold + reservedUnits < maxUnits);
  }

  public void reserve(int n) {
    if (n <= 0 || (maxUnits != null && n > maxUnits - unitsSold - reservedUnits))
      throw DomainException.conflict("No quedan suficientes unidades promocionales");
    reservedUnits += n;
  }

  public void release(int n) {
    if (n <= 0 || n > reservedUnits) throw DomainException.conflict("Reserva promocional inválida");
    reservedUnits -= n;
  }

  public void commit(int n) {
    release(n);
    unitsSold += n;
  }

  public void cancel() {
    cancelled = true;
    active = false;
  }
}
