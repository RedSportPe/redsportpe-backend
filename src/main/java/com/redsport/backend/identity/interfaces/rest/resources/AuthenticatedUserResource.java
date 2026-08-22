package com.redsport.backend.identity.interfaces.rest.resources;

public record AuthenticatedUserResource(
        String id,
        String name,
        String email,
        String role,
        String token
) { }