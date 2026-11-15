package com.redsport.backend.identity.application.ports;

import java.util.UUID;

public interface OperatorAccounts {
  UUID create(String name, String email, String password);

  void update(UUID id, String name, String email, String password);
}
