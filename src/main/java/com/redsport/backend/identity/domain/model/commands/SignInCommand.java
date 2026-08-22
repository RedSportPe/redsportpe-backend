package com.redsport.backend.identity.domain.model.commands;

public record SignInCommand(String email, String password) { }