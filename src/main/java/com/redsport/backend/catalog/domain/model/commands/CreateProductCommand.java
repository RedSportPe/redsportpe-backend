package com.redsport.backend.catalog.domain.model.commands;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
        String productCode,
        String name,
        String description,
        String category,
        BigDecimal price,
        boolean published,
        boolean featured,
        List<VariantData> variants,
        List<String> imageUrls
) {
    public record VariantData(String gender, String size, String color) { }
}