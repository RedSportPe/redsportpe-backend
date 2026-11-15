package com.redsport.backend.identity.application.internal;

import com.redsport.backend.identity.application.ports.OperatorAccounts;
import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.shared.domain.model.DomainException;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OperatorAccountService implements OperatorAccounts {
  private final UserCommandService commands;
  private final UserRepository users;
  private final PasswordEncoder passwords;

  public OperatorAccountService(
      UserCommandService commands, UserRepository users, PasswordEncoder passwords) {
    this.commands = commands;
    this.users = users;
    this.passwords = passwords;
  }

  public UUID create(String name, String email, String password) {
    return commands.handle(new CreateUserCommand(name, email, password, Roles.operator)).getId();
  }

  public void update(UUID id, String name, String email, String password) {
    var user =
        users
            .lockById(id)
            .filter(u -> u.isOperator())
            .orElseThrow(() -> DomainException.missing("Operadora no encontrada"));
    if (name != null && !name.isBlank()) user.updateName(name.trim());
    if (email != null && !email.isBlank()) {
      String normalized = email.trim().toLowerCase(Locale.ROOT);
      if (!normalized.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || normalized.length() > 160)
        throw new IllegalArgumentException("Correo inválido");
      if (!normalized.equals(user.getEmail()) && users.existsByEmail(normalized))
        throw DomainException.conflict("El correo ya está registrado");
      user.updateEmail(normalized);
    }
    if (password != null && !password.isBlank()) {
      if (password.length() < 8
          || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new IllegalArgumentException("Contraseña inválida");
      user.updatePasswordHash(passwords.encode(password));
    }
  }
}
