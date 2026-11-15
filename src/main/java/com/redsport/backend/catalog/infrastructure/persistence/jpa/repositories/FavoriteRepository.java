package com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.catalog.domain.model.aggregates.Favorite;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
  List<Favorite> findByUserId(UUID userId);

  Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId);
}
