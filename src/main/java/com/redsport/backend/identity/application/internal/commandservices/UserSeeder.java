package com.redsport.backend.identity.application.internal.commandservices;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the first admin at startup if no admin exists yet. Solves the chicken-and-egg problem: only
 * admins can create users, but there's no admin to start with. Runs once, safely.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "app.seed-admin.enabled",
    havingValue = "true")
public class UserSeeder {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @org.springframework.beans.factory.annotation.Value("${app.seed-admin.email:}")
  private String seedEmail;

  @org.springframework.beans.factory.annotation.Value("${app.seed-admin.password:}")
  private String seedPassword;

  @EventListener(ApplicationReadyEvent.class)
  public void seedAdmin() {
    // Only create if no admin exists — safe to run on every startup
    if (userRepository.existsByRole(Roles.admin)) {
      return;
    }

    if (seedEmail.isBlank() || seedPassword.length() < 12)
      throw new IllegalArgumentException(
          "Configura el correo y una contraseña de administrador de al menos 12 caracteres");
    String hashedPassword = passwordEncoder.encode(seedPassword);
    User admin = new User("Administrador RedSport", seedEmail, hashedPassword, Roles.admin);
    userRepository.save(admin);

    System.out.println("Administrador inicial creado");
  }
}
