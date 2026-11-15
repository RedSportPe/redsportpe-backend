package com.redsport.backend.orders.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.orders.domain.model.aggregates.Receipt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {}
