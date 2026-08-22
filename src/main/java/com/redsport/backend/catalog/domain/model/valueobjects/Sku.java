package com.redsport.backend.catalog.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Sku(@Column(name = "sku", length = 30) String value) {
    public Sku {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be empty");
        }
    }
    protected Sku() { this("RS-XXXX-U-M-NEG-T1"); }
}