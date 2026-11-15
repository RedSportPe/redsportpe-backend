package com.redsport.backend.orders.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.orders.domain.model.aggregates.SalesOrder;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<SalesOrder, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from SalesOrder o where o.id=:id")
  Optional<SalesOrder> lock(@Param("id") UUID id);

  Optional<SalesOrder> findByUserIdAndRequestKey(UUID userId, String requestKey);

  List<SalesOrder> findByUserIdOrderByCreatedAtDesc(UUID userId);

  List<SalesOrder> findByStoreCodeOrderByCreatedAtDesc(String storeCode);

  @Query("select o.id from SalesOrder o where o.status='PENDING' and o.qrExpiresAt<=:now")
  List<UUID> expired(@Param("now") Instant now);

  @Query(
      "select count(o)>0 from SalesOrder o join o.items i where i.productId=:id and o.status in"
          + " ('PENDING','PAID')")
  boolean hasActive(@Param("id") UUID id);
}
