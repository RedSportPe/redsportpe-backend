package com.redsport.backend.stores.application.internal.commandservices;

import com.redsport.backend.identity.application.ports.OperatorAccounts;
import com.redsport.backend.shared.application.SequenceService;
import com.redsport.backend.shared.domain.model.DomainException;
import com.redsport.backend.shared.infrastructure.persistence.*;
import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.commands.*;
import com.redsport.backend.stores.domain.model.entities.StoreOperator;
import com.redsport.backend.stores.domain.services.StoreCommandService;
import com.redsport.backend.stores.infrastructure.persistence.jpa.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StoreCommandServiceImpl implements StoreCommandService {
  private final StoreRepository stores;
  private final StoreOperatorRepository links;
  private final OperatorAccounts accounts;
  private final SequenceService sequence;
  private final SequenceRepository counters;

  public StoreCommandServiceImpl(
      StoreRepository stores,
      StoreOperatorRepository links,
      OperatorAccounts accounts,
      SequenceService sequence,
      SequenceRepository counters) {
    this.stores = stores;
    this.links = links;
    this.accounts = accounts;
    this.sequence = sequence;
    this.counters = counters;
  }

  public Store handle(CreateStoreWithOperatorCommand c) {
    long floor =
        stores.findAll().stream()
            .mapToLong(s -> Long.parseLong(s.getCode().substring(1)))
            .max()
            .orElse(0);
    long number = sequence.next("stores", floor);
    String code = "T" + number;
    var store =
        stores.save(
            new Store(
                c.name() == null || c.name().isBlank() ? "Tienda " + number : c.name(),
                code,
                String.format(java.util.Locale.ROOT, "B%03d", number),
                c.address(),
                c.managerName(),
                c.district(),
                c.province(),
                c.department(),
                c.phone()));
    counters.save(new SequenceCounter("receipt:" + code));
    var user = accounts.create(c.managerName(), c.operatorEmail(), c.operatorPassword());
    links.save(new StoreOperator(store.getId(), user));
    return store;
  }

  private Store active(java.util.UUID id) {
    return stores
        .findById(id)
        .filter(Store::isActive)
        .orElseThrow(() -> DomainException.missing("Tienda no encontrada"));
  }

  public Store handle(UpdateOperatorCommand c) {
    var store = active(c.storeId());
    var link =
        links
            .findByStoreId(store.getId())
            .orElseThrow(() -> DomainException.missing("Operadora no encontrada"));
    accounts.update(link.getUserId(), c.name(), c.email(), c.password());
    if (c.name() != null && !c.name().isBlank())
      store.updateDetails(
          store.getName(),
          store.getAddress(),
          c.name(),
          store.getDistrict(),
          store.getProvince(),
          store.getDepartment(),
          store.getPhone());
    return store;
  }

  public Store handle(UpdateStoreCommand c) {
    var store = active(c.id());
    store.updateDetails(
        c.name(),
        c.address(),
        c.managerName(),
        c.district(),
        c.province(),
        c.department(),
        c.phone());
    links
        .findByStoreId(store.getId())
        .ifPresent(link -> accounts.update(link.getUserId(), c.managerName(), null, null));
    return store;
  }

  public void handle(DeleteStoreCommand c) {
    stores
        .findById(c.storeId())
        .orElseThrow(() -> DomainException.missing("Tienda no encontrada"))
        .deactivate();
  }
}
