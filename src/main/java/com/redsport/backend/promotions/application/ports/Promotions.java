package com.redsport.backend.promotions.application.ports;

import java.math.BigDecimal;
import java.util.*;

public interface Promotions {
  Quote reserve(UUID productId, BigDecimal regularPrice, int quantity);

  void release(Map<UUID, Integer> quantities);

  void commit(Map<UUID, Integer> quantities);

  record Quote(BigDecimal unitPrice, UUID promotionId) {}
}
