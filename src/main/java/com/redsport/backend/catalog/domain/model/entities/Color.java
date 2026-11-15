package com.redsport.backend.catalog.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "colors")
@Getter
public class Color {
  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 3)
  private String code;

  @Column(nullable = false, length = 40)
  private String label;

  protected Color() {}

  public Color(String code, String label) {
    this.code = code;
    this.label = label;
  }
}
