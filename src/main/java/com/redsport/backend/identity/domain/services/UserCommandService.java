package com.redsport.backend.identity.domain.services;

import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.commands.SignInCommand;
import java.util.Optional;

public interface UserCommandService {
    Optional<ImmutablePair> handle(SignInCommand command);
    User handle(CreateUserCommand command);
    /** Small holder for the authenticated user + its token */
    record ImmutablePair(User user, String token) { }
}