package com.redsport.backend.catalog.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Locale;

@Embeddable
public record Sku(@Column(name = "sku", length = 60) String value) implements Serializable {
  public Sku {
    if (value == null) throw new IllegalArgumentException("SKU requerido");
    value = value.trim().toUpperCase(Locale.ROOT);
    if (!value.matches(
        "(RS|AD|NK)-[A-Z0-9]{1,10}-(H|M|U|NO|NA)-(S|M|L|XL|XXL|4|6|8|10|12|14|16)-[A-Z]{3}(?:-T[1-9][0-9]*)?"))
      throw new IllegalArgumentException("Formato de SKU inválido");
    String[] parts = value.split("-");
    boolean child = parts[2].equals("NO") || parts[2].equals("NA");
    if (child != parts[3].matches("[0-9]+"))
      throw new IllegalArgumentException("La talla no corresponde al género");
  }

  public String storeCode() {
    if (value.split("-").length != 6)
      throw new IllegalArgumentException("El SKU debe identificar una tienda");
    return value.substring(value.lastIndexOf('-') + 1);
  }
}
