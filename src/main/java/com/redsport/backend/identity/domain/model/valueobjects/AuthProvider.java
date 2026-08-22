package com.redsport.backend.identity.domain.model.valueobjects;

/** How the user authenticates. Matches the DB enum: 'email', 'google'. */
public enum AuthProvider {
    email,
    google
}