package com.redsport.backend.catalog.application.queries;

import java.math.BigDecimal;
import java.util.List;

public record ProductView(
    String id,
    String name,
    String description,
    String category,
    BigDecimal price,
    String imageUrl,
    List<String> images,
    boolean published,
    boolean featured,
    Integer salesCount,
    String createdAt,
    List<VariantView> variants) {}
