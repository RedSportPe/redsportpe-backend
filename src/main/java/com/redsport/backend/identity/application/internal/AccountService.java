package com.redsport.backend.identity.application.internal;

import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.shared.domain.model.DomainException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService implements CustomerAccounts {
  private final UserRepository users;
  private final com.redsport.backend.identity.infrastructure.persistence.jpa.repositories
          .DeliveryProfileRepository
      profiles;

  public AccountService(
      UserRepository users,
      com.redsport.backend.identity.infrastructure.persistence.jpa.repositories
              .DeliveryProfileRepository
          profiles) {
    this.users = users;
    this.profiles = profiles;
  }

  @Transactional(readOnly = true)
  public Account current(String email) {
    var u = users.findByEmail(email).orElseThrow(() -> new DomainException(401, "Sesión inválida"));
    return new Account(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getPoints());
  }

  @Transactional
  public void lock(UUID id) {
    users.lockById(id).orElseThrow(() -> DomainException.missing("Usuario no encontrado"));
  }

  @Transactional
  public void saveFirstDelivery(
      UUID id, String phone, String address, String district, String agency) {
    lock(id);
    if (!profiles.existsById(id))
      profiles.save(
          new com.redsport.backend.identity.domain.model.aggregates.DeliveryProfile(
              id, phone, address, district, agency));
  }

  @Transactional
  public void credit(UUID id, BigDecimal amount) {
    users
        .lockById(id)
        .orElseThrow(() -> DomainException.missing("Usuario no encontrado"))
        .creditPoints(amount);
  }
}
