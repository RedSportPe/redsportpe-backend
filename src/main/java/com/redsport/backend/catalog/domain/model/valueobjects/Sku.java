package com.redsport.backend.catalog.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * SKU is the Published Language between contexts. As a Value Object it validates
 * its own format (RS-[PRODUCT]-[GENDER]-[SIZE]-[COLOR]-[STORE]) and has no identity
 * beyond its value.
 */
@Embeddable
public record Sku(@Column(name = "sku") String value) {

    public Sku {
        if (value == null || !value.matches("^RS-[A-Z0-9]{2,6}-(H|M|U|NO|NA)-(8|10|12|14|16|S|M|L|XL|XXL)-[A-Z]{3}-T\\d+$")) {
            throw new IllegalArgumentException("Invalid SKU format: " + value);
        }
    }

    // JPA needs a no-arg path; records handle this via the canonical constructor.
    public Sku() { this("RS-XXXX-U-M-NEG-T1"); }
}