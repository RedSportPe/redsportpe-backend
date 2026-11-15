package com.redsport.backend.shipping.domain.services;

import java.time.*;

public final class DeliveryRules {
  private DeliveryRules() {}

  public static LocalDate deliveryDate(String method, Instant paidAt) {
    var local = paidAt.atZone(ZoneId.of("America/Lima"));
    boolean before = local.toLocalTime().isBefore(LocalTime.of(15, 0));
    return local
        .toLocalDate()
        .plusDays(
            switch (method) {
              case "motorizado" -> before ? 1 : 2;
              case "shalom" -> before ? 0 : 1;
              case "tienda" -> 0;
              default -> throw new IllegalArgumentException("Método de entrega inválido");
            });
  }
}
