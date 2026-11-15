package com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.catalog.domain.model.entities.GarmentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarmentTypeRepository extends JpaRepository<GarmentType, String> {}
