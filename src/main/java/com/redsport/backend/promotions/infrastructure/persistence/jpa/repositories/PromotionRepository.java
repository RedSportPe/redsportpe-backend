package com.redsport.backend.promotions.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.promotions.domain.model.aggregates.Promotion;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Promotion p where p.productId=:id order by p.id")
  List<Promotion> lockByProduct(@Param("id") UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Promotion p where p.id=:id")
  Optional<Promotion> lock(@Param("id") UUID id);
}
