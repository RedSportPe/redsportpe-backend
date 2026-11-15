package com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  List<Product> findByPublishedTrueAndDeletedFalse();

  List<Product> findByDeletedFalse();

  @Query("select distinct p from Product p join p.variants v where v.sku.value=:sku")
  Optional<Product> findBySku(@Param("sku") String sku);

  @Query("select p.id from Product p join p.variants v where v.sku.value=:sku")
  Optional<UUID> findProductIdBySku(@Param("sku") String sku);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id=:id")
  Optional<Product> lockById(@Param("id") UUID id);
}
