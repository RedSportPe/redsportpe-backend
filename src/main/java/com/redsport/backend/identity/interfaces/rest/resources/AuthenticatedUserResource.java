package com.redsport.backend.identity.interfaces.rest.resources;

public record AuthenticatedUserResource(
    String id,
    String name,
    String email,
    String role,
    java.math.BigDecimal points,
    String storeCode,
    String provider,
    String createdAt,
    String token) {}
