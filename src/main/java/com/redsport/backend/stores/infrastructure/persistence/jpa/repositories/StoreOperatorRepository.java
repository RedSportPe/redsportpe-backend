package com.redsport.backend.stores.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.stores.domain.model.entities.StoreOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreOperatorRepository extends JpaRepository<StoreOperator, UUID> {
    Optional<StoreOperator> findByStoreId(UUID storeId);
}