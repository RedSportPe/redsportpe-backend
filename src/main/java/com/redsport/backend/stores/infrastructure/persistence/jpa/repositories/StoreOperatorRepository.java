package com.redsport.backend.stores.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.stores.domain.model.entities.StoreOperator;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreOperatorRepository
    extends JpaRepository<StoreOperator, StoreOperator.StoreOperatorId> {
  Optional<StoreOperator> findByUserId(UUID userId);

  Optional<StoreOperator> findByStoreId(UUID storeId);
}
