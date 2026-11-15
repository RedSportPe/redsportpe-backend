package com.redsport.backend.payments.interfaces.rest;

import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.shared.domain.model.DomainException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentInstructionsController {
  private final OrderService orders;
  private final String image, recipient;
  private final Clock clock;

  public PaymentInstructionsController(
      OrderService orders,
      Clock clock,
      @Value("${app.payment.qr-image-url:}") String image,
      @Value("${app.payment.recipient:}") String recipient) {
    this.orders = orders;
    this.clock = clock;
    this.image = image;
    this.recipient = recipient;
  }

  @GetMapping("/api/orders/{id}/payment-instructions")
  public Object instructions(Principal p, @PathVariable UUID id) {
    var order = orders.get(p.getName(), id);
    order.requirePayable(clock.instant());
    if (image.isBlank() || recipient.isBlank())
      throw new DomainException(503, "Configura el QR y beneficiario de la tienda");
    return new Instructions(
        order.getId(),
        order.getTotal(),
        "PEN",
        recipient,
        image,
        order.getQrExpiresAt(),
        "manual-review");
  }

  public record Instructions(
      UUID orderId,
      BigDecimal amount,
      String currency,
      String recipient,
      String qrImageUrl,
      Instant expiresAt,
      String verificationMode) {}
}
