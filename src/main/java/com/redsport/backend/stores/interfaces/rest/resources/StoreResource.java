package com.redsport.backend.stores.interfaces.rest.resources;

public record StoreResource(
        String id,
        String name,
        String code,
        String boletaSeries,
        String address,
        String managerName,
        String district,
        String province,
        String department,
        String phone,
        boolean active
) { }