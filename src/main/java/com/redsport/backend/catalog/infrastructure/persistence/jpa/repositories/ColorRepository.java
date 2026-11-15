package com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.catalog.domain.model.entities.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, String> {}
