package com.redsport.backend.catalog.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "garment_types")
@Getter
public class GarmentType {
  @Id
  @Column(length = 30)
  private String code;

  @Column(nullable = false, length = 60)
  private String label;

  private boolean active = true;

  protected GarmentType() {}

  public GarmentType(String code, String label) {
    this.code = code;
    this.label = label;
  }
}
