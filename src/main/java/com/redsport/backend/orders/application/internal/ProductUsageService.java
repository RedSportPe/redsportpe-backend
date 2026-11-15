package com.redsport.backend.orders.application.internal;

import com.redsport.backend.catalog.application.ports.ProductUsage;
import com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductUsageService implements ProductUsage {
  private final OrderRepository orders;

  public ProductUsageService(OrderRepository orders) {
    this.orders = orders;
  }

  public boolean hasActiveOrders(UUID id) {
    return orders.hasActive(id);
  }
}
