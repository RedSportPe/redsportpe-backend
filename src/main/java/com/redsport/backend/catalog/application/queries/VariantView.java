package com.redsport.backend.catalog.application.queries;

public record VariantView(
    String sku, String gender, String size, String color, Integer totalStock) {}
