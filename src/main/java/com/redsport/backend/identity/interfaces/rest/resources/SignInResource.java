package com.redsport.backend.identity.interfaces.rest.resources;

import jakarta.validation.constraints.*;

public record SignInResource(@NotBlank @Email String email, @NotBlank String password) {}
