package com.redsport.backend.identity.application.internal.commandservices;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.commands.SignInCommand;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCommandServiceImpl implements UserCommandService {

  private final com.redsport.backend.stores.application.ports.StoreAccess storeAccess;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  public UserCommandServiceImpl(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenService tokenService,
      com.redsport.backend.stores.application.ports.StoreAccess storeAccess) {
    this.storeAccess = storeAccess;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
  }

  @Override
  public Optional<ImmutablePair> handle(SignInCommand command) {
    var user =
        userRepository.findByEmail(command.email().trim().toLowerCase(java.util.Locale.ROOT));
    if (user.isEmpty()) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Credenciales inválidas");
    }

    var found = user.get();
    if (found.getPasswordHash() == null
        || !passwordEncoder.matches(command.password(), found.getPasswordHash())) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Credenciales inválidas");
    }

    if (found.isOperator()) storeAccess.requireOperator(found.getEmail());
    String token = tokenService.generateToken(found.getEmail(), found.getRole().name());
    return Optional.of(new ImmutablePair(found, token));
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public User handle(CreateUserCommand command) {
    if (command.email() == null || !command.email().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
      throw new IllegalArgumentException("Correo inválido");
    if (command.name() == null || command.name().isBlank() || command.name().length() > 120)
      throw new IllegalArgumentException("Nombre inválido");
    if (command.password() == null
        || command.password().length() < 8
        || command.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException(
          "La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes");
    if (command.role() == null) throw new IllegalArgumentException("Rol requerido");
    String email = command.email().trim().toLowerCase(java.util.Locale.ROOT);
    // Invariant: no two users with the same email
    if (userRepository.existsByEmail(email)) {
      throw com.redsport.backend.shared.domain.model.DomainException.conflict(
          "Ya existe un usuario con ese correo");
    }
    String hashedPassword = passwordEncoder.encode(command.password());
    User user = new User(command.name().trim(), email, hashedPassword, command.role());
    return userRepository.save(user);
  }
}
