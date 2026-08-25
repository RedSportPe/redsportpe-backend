package com.redsport.backend.stores.domain.model.commands;

import java.util.UUID;

public record DeleteStoreCommand(UUID storeId) { }