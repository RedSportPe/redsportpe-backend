package com.redsport.backend.stores.interfaces.rest.resources;

public record UpdateOperatorResource(
        String name,
        String email,
        String password
) { }