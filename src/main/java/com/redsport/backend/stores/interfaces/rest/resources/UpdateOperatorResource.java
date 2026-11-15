package com.redsport.backend.stores.interfaces.rest.resources;

import jakarta.validation.constraints.*;

public record UpdateOperatorResource(
    @Size(max = 120) String name,
    @Email @Size(max = 160) String email,
    @Size(max = 72) String password) {}
