package com.redsport.backend.identity.interfaces.rest.transform;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }
}