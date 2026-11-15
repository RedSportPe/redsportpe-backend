package com.redsport.backend.catalog.domain.services;

import com.redsport.backend.catalog.domain.model.valueobjects.Sku;

public final class SkuGenerator {
  private SkuGenerator() {}

  public static String generate(
      String brand,
      String productCode,
      String gender,
      String size,
      String color,
      String storeCode) {
    return new Sku(String.join("-", brand, productCode, gender, size, color, storeCode)).value();
  }
}
