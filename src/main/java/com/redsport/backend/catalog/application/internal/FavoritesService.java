package com.redsport.backend.catalog.application.internal;

import com.redsport.backend.catalog.application.ports.Catalog;
import com.redsport.backend.catalog.domain.model.aggregates.Favorite;
import com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories.FavoriteRepository;
import com.redsport.backend.identity.application.ports.CustomerAccounts;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoritesService {
  private final FavoriteRepository favorites;
  private final CustomerAccounts accounts;
  private final Catalog catalog;

  public FavoritesService(
      FavoriteRepository favorites, CustomerAccounts accounts, Catalog catalog) {
    this.favorites = favorites;
    this.accounts = accounts;
    this.catalog = catalog;
  }

  @Transactional(readOnly = true)
  public List<UUID> list(String email) {
    return favorites.findByUserId(accounts.current(email).id()).stream()
        .map(Favorite::getProductId)
        .toList();
  }

  public void set(String email, UUID id, boolean value) {
    var user = accounts.current(email);
    accounts.lock(user.id());
    var existing = favorites.findByUserIdAndProductId(user.id(), id);
    if (value) {
      catalog.requirePublished(id);
      if (existing.isEmpty()) favorites.save(new Favorite(user.id(), id));
    } else existing.ifPresent(favorites::delete);
  }
}
