package com.redsport.backend.identity.application.ports;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerAccounts {
  Account current(String email);

  void lock(UUID id);

  void saveFirstDelivery(UUID id, String phone, String address, String district, String agency);

  void credit(UUID userId, BigDecimal points);

  record Account(UUID id, String name, String email, String role, BigDecimal points) {}
}
