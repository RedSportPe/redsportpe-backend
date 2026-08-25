package com.redsport.backend.stores.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.stores.domain.model.aggregates.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    // Only active stores (soft-deleted ones stay hidden but preserved)
    List<Store> findByActiveTrue();

    // For generating the next code: how many stores ever existed (incl. inactive)
    long count();
}