package com.redsport.backend.identity.domain.model.commands;

import com.redsport.backend.identity.domain.model.valueobjects.Roles;

public record CreateUserCommand(String name, String email, String password, Roles role) { }