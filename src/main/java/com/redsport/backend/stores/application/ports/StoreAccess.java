package com.redsport.backend.stores.application.ports;

import java.util.Optional;
import java.util.UUID;

public interface StoreAccess {
  Optional<StoreInfo> assignedStore(String email);

  StoreInfo requireOperator(String email);

  void requireActive(String code);

  record StoreInfo(
      UUID id, String code, String name, String boletaSeries, String address, String managerName) {}
}
