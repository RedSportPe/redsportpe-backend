package com.redsport.backend.payments.application.ports;

import java.math.BigDecimal;
import java.util.UUID;

public interface Payments {
  void initialize(UUID orderId);

  void submit(UUID orderId, String reference);

  boolean confirm(
      UUID orderId, String reference, BigDecimal expected, BigDecimal received, String admin);

  void reset(UUID orderId);

  void reject(UUID orderId, String admin, String note);

  Object status(UUID orderId);
}
