package com.redsport.backend.identity.application.internal.commandservices;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the first admin at startup if no admin exists yet.
 * Solves the chicken-and-egg problem: only admins can create users,
 * but there's no admin to start with. Runs once, safely.
 */
@Component
public class UserSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedAdmin() {
        // Only create if no admin exists — safe to run on every startup
        if (userRepository.existsByRole(Roles.admin)) {
            return;
        }

        String hashedPassword = passwordEncoder.encode("admin123");
        User admin = new User("Administrador RedSport", "admin@redsport.pe", hashedPassword, Roles.admin);
        userRepository.save(admin);

        System.out.println("✓ Admin seeded: admin@redsport.pe / admin123");
    }
}