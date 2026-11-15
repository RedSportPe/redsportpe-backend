package com.redsport.backend.identity.application.internal;

import com.redsport.backend.identity.application.ports.GoogleIdentityTokens;
import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.services.UserCommandService.ImmutablePair;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import com.redsport.backend.shared.domain.model.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleAuthenticationService {
  private final UserRepository users;
  private final TokenService tokens;
  private final GoogleIdentityTokens identities;

  public GoogleAuthenticationService(
      UserRepository users, TokenService tokens, GoogleIdentityTokens identities) {
    this.users = users;
    this.tokens = tokens;
    this.identities = identities;
  }

  @Transactional
  public ImmutablePair authenticate(String credential) {
    var identity = identities.verify(credential);
    var existing = users.findByGoogleSubject(identity.subject());
    User user;
    if (existing.isPresent()) user = existing.get();
    else {
      if (users.existsByEmail(identity.email()))
        throw DomainException.conflict(
            "Inicia sesión con tu contraseña; la vinculación de cuentas requiere verificar la"
                + " sesión existente");
      String name = identity.name();
      if (name.length() > 120) name = name.substring(0, 120);
      user = users.save(User.google(name, identity.email(), identity.subject()));
    }
    return new ImmutablePair(user, tokens.generateToken(user.getEmail(), user.getRole().name()));
  }
}
