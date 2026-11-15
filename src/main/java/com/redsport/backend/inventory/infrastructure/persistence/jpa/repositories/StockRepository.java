package com.redsport.backend.inventory.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.inventory.domain.model.aggregates.StockItem;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<StockItem, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from StockItem s where s.sku=:sku")
  Optional<StockItem> lock(@Param("sku") String sku);
}
