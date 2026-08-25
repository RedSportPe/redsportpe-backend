package com.redsport.backend.stores.domain.model.commands;

import java.util.UUID;

/** Change a store's cashier credential. Password is optional (blank = keep current). */
public record UpdateOperatorCommand(
        UUID storeId,
        String name,
        String email,
        String password
) { }