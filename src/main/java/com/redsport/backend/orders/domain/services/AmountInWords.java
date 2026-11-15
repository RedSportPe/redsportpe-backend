package com.redsport.backend.orders.domain.services;

import java.math.BigDecimal;
import java.util.Locale;

public final class AmountInWords {
  private AmountInWords() {}

  private static final String[] SMALL = {
    "CERO",
    "UNO",
    "DOS",
    "TRES",
    "CUATRO",
    "CINCO",
    "SEIS",
    "SIETE",
    "OCHO",
    "NUEVE",
    "DIEZ",
    "ONCE",
    "DOCE",
    "TRECE",
    "CATORCE",
    "QUINCE",
    "DIECISÉIS",
    "DIECISIETE",
    "DIECIOCHO",
    "DIECINUEVE",
    "VEINTE",
    "VEINTIUNO",
    "VEINTIDÓS",
    "VEINTITRÉS",
    "VEINTICUATRO",
    "VEINTICINCO",
    "VEINTISÉIS",
    "VEINTISIETE",
    "VEINTIOCHO",
    "VEINTINUEVE"
  };

  public static String soles(BigDecimal amount) {
    var value = com.redsport.backend.shared.domain.model.Money.amount(amount);
    long whole = value.longValue();
    if (whole >= 1000000000L)
      throw new IllegalArgumentException("El importe supera el límite del comprobante");
    int cents = value.remainder(BigDecimal.ONE).movePointRight(2).intValueExact();
    return apocope(number((int) whole))
        + " CON "
        + String.format(Locale.ROOT, "%02d", cents)
        + "/100 SOLES";
  }

  private static String apocope(String s) {
    return s.endsWith("VEINTIUNO")
        ? s.substring(0, s.length() - 9) + "VEINTIÚN"
        : s.endsWith("UNO") ? s.substring(0, s.length() - 3) + "UN" : s;
  }

  private static String number(int n) {
    if (n < 30) return SMALL[n];
    if (n < 100) {
      String t =
          new String[] {
                "",
                "",
                "",
                "TREINTA",
                "CUARENTA",
                "CINCUENTA",
                "SESENTA",
                "SETENTA",
                "OCHENTA",
                "NOVENTA"
              }
              [n / 10];
      return t + (n % 10 == 0 ? "" : " Y " + number(n % 10));
    }
    if (n == 100) return "CIEN";
    if (n < 1000) {
      String h =
          new String[] {
                "",
                "CIENTO",
                "DOSCIENTOS",
                "TRESCIENTOS",
                "CUATROCIENTOS",
                "QUINIENTOS",
                "SEISCIENTOS",
                "SETECIENTOS",
                "OCHOCIENTOS",
                "NOVECIENTOS"
              }
              [n / 100];
      return h + (n % 100 == 0 ? "" : " " + number(n % 100));
    }
    if (n < 1000000)
      return (n / 1000 == 1 ? "MIL" : apocope(number(n / 1000)) + " MIL")
          + (n % 1000 == 0 ? "" : " " + number(n % 1000));
    return (n / 1000000 == 1 ? "UN MILLÓN" : apocope(number(n / 1000000)) + " MILLONES")
        + (n % 1000000 == 0 ? "" : " " + number(n % 1000000));
  }
}
