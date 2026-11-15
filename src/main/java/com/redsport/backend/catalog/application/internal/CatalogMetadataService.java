package com.redsport.backend.catalog.application.internal;

import com.redsport.backend.catalog.domain.model.entities.*;
import com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogMetadataService {
  private final ColorRepository colors;
  private final GarmentTypeRepository types;

  public CatalogMetadataService(ColorRepository colors, GarmentTypeRepository types) {
    this.colors = colors;
    this.types = types;
  }

  @Transactional(readOnly = true)
  public Object colors() {
    return colors.findAll();
  }

  @Transactional(readOnly = true)
  public Object types() {
    return types.findAll();
  }

  public Object color(String code, String label) {
    if (!code.matches("[A-Z]{3}") || label == null || label.isBlank() || label.length() > 40)
      throw new IllegalArgumentException("Color inválido");
    return colors.save(new Color(code, label));
  }

  public Object type(String code, String label) {
    if (!code.matches("[A-Z0-9]{1,30}") || label == null || label.isBlank() || label.length() > 60)
      throw new IllegalArgumentException("Tipo de prenda inválido");
    return types.save(new GarmentType(code, label));
  }
}
