package com.redsport.backend.catalog.application.ports;

import java.util.UUID;

public interface ProductUsage {
  boolean hasActiveOrders(UUID productId);
}
