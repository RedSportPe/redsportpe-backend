package com.redsport.backend.catalog.domain.model.entities;

import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "variants")
@Getter
public class Variant {

    @EmbeddedId
    private Sku sku;

    @Column(name = "gender", length = 2)
    private String gender;

    @Column(name = "size", length = 4)
    private String size;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "color", length = 3)
    private String color;

    protected Variant() { }
}