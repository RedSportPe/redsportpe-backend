package com.redsport.backend.identity.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.identity.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(com.redsport.backend.identity.domain.model.valueobjects.Roles role);
}