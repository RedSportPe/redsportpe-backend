package com.redsport.backend.catalog.interfaces.rest.resources;

public record VariantResource(
        String sku,
        String gender,
        String size,
        String color,
        Integer totalStock
) { }