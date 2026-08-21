package com.redsport.backend.catalog.domain.model.entities;

import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Variant is an entity inside the Product aggregate. It's never accessed or
 * persisted on its own — always through its aggregate root (Product).
 */
@Entity
@Table(name = "variants")
@Getter
public class Variant {

    @EmbeddedId
    private Sku sku;

    @Column(name = "gender", length = 2, nullable = false)
    private String gender;

    @Column(name = "size", length = 4, nullable = false)
    private String size;

    @Column(name = "color", length = 3, nullable = false)
    private String color;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    protected Variant() { } // required by JPA

    public boolean isAvailable() {
        return totalStock != null && totalStock > 0;
    }
}