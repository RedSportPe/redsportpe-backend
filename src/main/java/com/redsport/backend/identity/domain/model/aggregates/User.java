package com.redsport.backend.identity.domain.model.aggregates;

import com.redsport.backend.identity.domain.model.valueobjects.AuthProvider;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * User is the aggregate root of the Identity context. Single table with a role
 * discriminator (customer/admin/operator) — a role change is one UPDATE, not a
 * row migration between tables.
 */
@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "email", length = 160, nullable = false, unique = true)
    private String email;

    /** Always stored hashed (BCrypt). Null when provider = google. */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Roles role;

    @Column(name = "points", nullable = false)
    private BigDecimal points;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    protected User() { } // required by JPA

    /** Factory: create an internal/email user with a role (admin creates operators this way) */
    public User(String name, String email, String passwordHash, Roles role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.provider = AuthProvider.email;
        this.points = BigDecimal.ZERO;
        this.createdAt = OffsetDateTime.now();
    }

    // ===== Behaviour =====

    public boolean isAdmin() {
        return role == Roles.admin;
    }

    public boolean isOperator() {
        return role == Roles.operator;
    }
}