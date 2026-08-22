package com.redsport.backend.identity.interfaces.rest.resources;

public record CreateUserResource(String name, String email, String password, String role) { }