package com.redsport.backend.orders.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.orders.domain.model.entities.SavedCartLine;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<SavedCartLine, UUID> {
  List<SavedCartLine> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}
