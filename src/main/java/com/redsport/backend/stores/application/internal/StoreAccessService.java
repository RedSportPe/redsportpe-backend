package com.redsport.backend.stores.application.internal;

import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.shared.domain.model.DomainException;
import com.redsport.backend.stores.application.ports.StoreAccess;
import com.redsport.backend.stores.infrastructure.persistence.jpa.repositories.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoreAccessService implements StoreAccess {
  private final StoreRepository stores;
  private final StoreOperatorRepository links;
  private final CustomerAccounts accounts;

  public StoreAccessService(
      StoreRepository stores, StoreOperatorRepository links, CustomerAccounts accounts) {
    this.stores = stores;
    this.links = links;
    this.accounts = accounts;
  }

  public Optional<StoreInfo> assignedStore(String email) {
    var user = accounts.current(email);
    return links
        .findByUserId(user.id())
        .flatMap(l -> stores.findById(l.getStoreId()))
        .filter(s -> s.isActive())
        .map(
            s ->
                new StoreInfo(
                    s.getId(),
                    s.getCode(),
                    s.getName(),
                    s.getBoletaSeries(),
                    s.getAddress(),
                    s.getManagerName()));
  }

  public StoreInfo requireOperator(String email) {
    if (!accounts.current(email).role().equals("operator")) throw DomainException.forbidden();
    return assignedStore(email)
        .orElseThrow(() -> new DomainException(403, "La tienda no está activa"));
  }

  public void requireActive(String code) {
    if (stores.findByCode(code).filter(s -> s.isActive()).isEmpty())
      throw DomainException.conflict("La tienda del SKU no está activa");
  }
}
