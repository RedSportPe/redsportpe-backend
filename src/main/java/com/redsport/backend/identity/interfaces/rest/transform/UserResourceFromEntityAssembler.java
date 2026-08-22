package com.redsport.backend.identity.interfaces.rest.transform;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}