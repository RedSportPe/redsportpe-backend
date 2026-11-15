package com.redsport.backend.payments.application.internal;

import com.redsport.backend.payments.application.ports.Payments;
import com.redsport.backend.payments.domain.model.aggregates.Payment;
import com.redsport.backend.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import com.redsport.backend.shared.domain.model.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mutations are invoked only while the owning order is locked by OrderService. */
@Service
@Transactional
public class PaymentService implements Payments {
  private final PaymentRepository payments;
  private final Clock clock;

  public PaymentService(PaymentRepository payments, Clock clock) {
    this.payments = payments;
    this.clock = clock;
  }

  public void initialize(UUID id) {
    payments.save(new Payment(id));
  }

  private Payment get(UUID id) {
    return payments.findById(id).orElseThrow(() -> DomainException.missing("Pago no encontrado"));
  }

  private String reference(String value) {
    if (value == null || !value.trim().matches("[a-zA-Z0-9_-]{4,100}"))
      throw new IllegalArgumentException("Número de operación inválido");
    return value.trim().toUpperCase(java.util.Locale.ROOT);
  }

  public void submit(UUID id, String reference) {
    get(id).submit(reference(reference), clock.instant());
  }

  public boolean confirm(
      UUID id, String reference, BigDecimal expected, BigDecimal received, String admin) {
    String ref = reference(reference);
    var payment = get(id);
    if (Money.positive(received).compareTo(expected) != 0)
      throw DomainException.conflict("El importe recibido no coincide con el pedido");
    if (payment.getStatus().equals("PAID")) {
      if (!ref.equals(payment.getOperationReference()))
        throw DomainException.conflict("El pedido ya tiene otra operación confirmada");
      return false;
    }
    payment.confirm(ref, received, admin, clock.instant());
    payments.flush();
    return true;
  }

  public void reject(UUID id, String admin, String note) {
    get(id).reject(admin, note);
  }

  public void reset(UUID id) {
    get(id).reset();
  }

  @Transactional(readOnly = true)
  public Object status(UUID id) {
    return get(id);
  }
}
