package com.redsport.backend.stores.domain.model.commands;

/** Everything needed to create a store AND its operator credential in one shot. */
public record CreateStoreWithOperatorCommand(
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