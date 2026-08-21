package com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByPublishedTrue();
}