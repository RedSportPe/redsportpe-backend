package com.redsport.backend.payments.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.payments.domain.model.aggregates.Payment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {}
