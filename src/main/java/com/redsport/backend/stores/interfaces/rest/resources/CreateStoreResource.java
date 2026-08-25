package com.redsport.backend.stores.interfaces.rest.resources;

public record CreateStoreResource(
        String name,
        String address,
        String managerName,
        String district,
        String province,
        String department,
        String phone,
        String operatorEmail,
        String operatorPassword
) { }