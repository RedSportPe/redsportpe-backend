package com.redsport.backend.identity.application.internal;

import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.identity.domain.model.aggregates.DeliveryProfile;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.DeliveryProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
  private final CustomerAccounts accounts;
  private final DeliveryProfileRepository profiles;

  public ProfileService(CustomerAccounts accounts, DeliveryProfileRepository profiles) {
    this.accounts = accounts;
    this.profiles = profiles;
  }

  @Transactional(readOnly = true)
  public DeliveryProfile get(String email) {
    return profiles.findById(accounts.current(email).id()).orElse(null);
  }

  @Transactional
  public DeliveryProfile save(
      String email, String phone, String address, String district, String agency) {
    return profiles.save(
        new DeliveryProfile(accounts.current(email).id(), phone, address, district, agency));
  }
}
