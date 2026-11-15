package com.redsport.backend.identity.interfaces.rest.resources;

import jakarta.validation.constraints.*;

public record CreateUserResource(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 160) String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String role) {}
