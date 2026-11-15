package com.redsport.backend.stores.domain.model.commands;

import java.util.UUID;

public record UpdateStoreCommand(
    UUID id,
    String name,
    String address,
    String managerName,
    String district,
    String province,
    String department,
    String phone) {}
