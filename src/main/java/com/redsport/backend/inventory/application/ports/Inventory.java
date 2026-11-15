package com.redsport.backend.inventory.application.ports;

import java.util.Map;

public interface Inventory {
  int available(String sku);

  void initialize(String sku, int quantity);

  void reserve(Map<String, Integer> quantities);

  void release(Map<String, Integer> quantities);

  void commit(Map<String, Integer> quantities);
}
