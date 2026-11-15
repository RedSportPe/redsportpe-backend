package com.redsport.backend.stores.interfaces.rest.resources;

import jakarta.validation.constraints.*;

public record CreateStoreResource(
    @Size(max = 100) String name,
    @Size(max = 200) String address,
    @NotBlank @Size(max = 120) String managerName,
    @Size(max = 100) String district,
    @Size(max = 100) String province,
    @Size(max = 100) String department,
    @Size(max = 20) String phone,
    @NotBlank @Email @Size(max = 160) String operatorEmail,
    @NotBlank @Size(min = 8, max = 72) String operatorPassword) {}
