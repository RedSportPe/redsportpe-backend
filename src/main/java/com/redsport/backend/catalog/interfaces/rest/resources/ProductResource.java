package com.redsport.backend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record ProductResource(
        String id,
        String name,
        String description,
        String category,
        BigDecimal price,
        String imageUrl,
        boolean published,
        boolean featured,
        Integer salesCount,
        String createdAt,
        List<VariantResource> variants
) { }