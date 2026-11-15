package com.redsport.backend.payments.domain.model.aggregates;

import com.redsport.backend.shared.domain.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "payment_reviews")
@Getter
public class Payment {
  @Id private UUID orderId;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(unique = true, length = 100)
  private String operationReference;

  @Column(precision = 12, scale = 2)
  private BigDecimal amount;

  private Instant submittedAt;
  private Instant confirmedAt;

  @Column(length = 160)
  private String reviewedBy;

  @Column(length = 300)
  private String reviewNote;

  public void reject(String admin, String note) {
    if (status.equals("PAID")) throw DomainException.conflict("Pago confirmado");
    reset();
    status = "REJECTED";
    reviewedBy = admin;
    reviewNote = note;
  }

  protected Payment() {}

  public Payment(UUID id) {
    orderId = id;
    status = "PENDING";
  }

  public void submit(String reference, Instant now) {
    if (status.equals("PAID")) throw DomainException.conflict("Pago ya confirmado");
    operationReference = reference;
    submittedAt = now;
    status = "REVIEW";
  }

  public void confirm(String reference, BigDecimal amount, String admin, Instant now) {
    if (operationReference != null && !operationReference.equals(reference))
      throw DomainException.conflict("La operación no coincide con la informada por el cliente");
    operationReference = reference;
    this.amount = Money.positive(amount);
    reviewedBy = admin;
    confirmedAt = now;
    status = "PAID";
  }

  public void reset() {
    if (status.equals("PAID")) throw DomainException.conflict("Pago confirmado");
    status = "PENDING";
    operationReference = null;
    submittedAt = null;
  }
}
