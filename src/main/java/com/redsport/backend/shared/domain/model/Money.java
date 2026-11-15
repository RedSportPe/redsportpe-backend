package com.redsport.backend.shared.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
  private Money() {}

  public static BigDecimal amount(BigDecimal value) {
    if (value == null || value.signum() < 0 || value.scale() > 2)
      throw new IllegalArgumentException(
          "El importe debe ser positivo o cero y tener como máximo dos decimales");
    return value.setScale(2, RoundingMode.UNNECESSARY);
  }

  public static BigDecimal positive(BigDecimal value) {
    var result = amount(value);
    if (result.signum() == 0)
      throw new IllegalArgumentException("El importe debe ser mayor a cero");
    return result;
  }
}
