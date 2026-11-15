package com.redsport.backend.catalog.application.ports;

import java.math.BigDecimal;
import java.util.*;

public interface Catalog {
  void requirePublished(UUID id);

  Item readItem(String sku);

  List<Item> lockItems(Collection<String> skus);

  BigDecimal lockPrice(UUID productId);

  void recordSale(UUID productId, int quantity);

  record Item(String sku, UUID productId, String name, BigDecimal price, String imageUrl) {}
}
