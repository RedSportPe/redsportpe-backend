package com.redsport.backend.identity.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.identity.domain.model.aggregates.DeliveryProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProfileRepository extends JpaRepository<DeliveryProfile, UUID> {}
