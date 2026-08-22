package com.redsport.backend.identity.domain.model.valueobjects;

/** The role discriminator for the single users table.
 *  Matches the DB enum: 'customer', 'admin', 'operator'. */
public enum Roles {
    customer,
    admin,
    operator
}