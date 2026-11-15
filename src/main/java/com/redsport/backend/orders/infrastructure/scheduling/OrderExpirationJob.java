package com.redsport.backend.orders.infrastructure.scheduling;

import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(
    name = "app.order-expiration.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class OrderExpirationJob {
  private final OrderService service;
  private final OrderRepository orders;
  private final Clock clock;
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(OrderExpirationJob.class);

  public OrderExpirationJob(OrderService service, OrderRepository orders, Clock clock) {
    this.service = service;
    this.orders = orders;
    this.clock = clock;
  }

  @Scheduled(fixedDelayString = "${app.order-expiration.delay-ms:60000}")
  public void expire() {
    for (var id : orders.expired(clock.instant()))
      try {
        service.expire(id);
      } catch (RuntimeException e) {
        log.warn("No se pudo liberar el pedido {}: {}", id, e.getClass().getSimpleName());
      }
  }
}
