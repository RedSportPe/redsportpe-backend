package com.redsport.backend.stores.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record StoreCode(@Column(name = "code", length = 10) String value) {
    public StoreCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Store code cannot be empty");
        }
    }
    protected StoreCode() { this("T0"); }
}