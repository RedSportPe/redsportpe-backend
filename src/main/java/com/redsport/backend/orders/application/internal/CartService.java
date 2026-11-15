package com.redsport.backend.orders.application.internal;

import com.redsport.backend.catalog.application.ports.Catalog;
import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.inventory.application.ports.Inventory;
import com.redsport.backend.orders.domain.model.entities.SavedCartLine;
import com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.CartRepository;
import com.redsport.backend.shared.domain.model.DomainException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
  private final CartRepository carts;
  private final CustomerAccounts accounts;
  private final Catalog catalog;
  private final Inventory inventory;

  public CartService(
      CartRepository carts, CustomerAccounts accounts, Catalog catalog, Inventory inventory) {
    this.carts = carts;
    this.accounts = accounts;
    this.catalog = catalog;
    this.inventory = inventory;
  }

  @Transactional(readOnly = true)
  public List<SavedCartLine> get(String email) {
    return carts.findByUserId(accounts.current(email).id());
  }

  public List<SavedCartLine> save(String email, List<Line> lines) {
    var id = accounts.current(email).id();
    accounts.lock(id);
    Set<String> seen = new HashSet<>();
    List<SavedCartLine> result = new ArrayList<>();
    for (var line : lines) {
      String sku = new Sku(line.sku()).value();
      if (!seen.add(sku)) throw new IllegalArgumentException("SKU repetido");
      catalog.readItem(sku);
      if (line.quantity() > inventory.available(sku))
        throw DomainException.conflict("Stock insuficiente: " + sku);
      result.add(new SavedCartLine(id, sku, line.quantity()));
    }
    carts.deleteByUserId(id);
    carts.flush();
    return carts.saveAll(result);
  }

  public record Line(
      @jakarta.validation.constraints.NotBlank String sku,
      @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(9999)
          int quantity) {}
}
