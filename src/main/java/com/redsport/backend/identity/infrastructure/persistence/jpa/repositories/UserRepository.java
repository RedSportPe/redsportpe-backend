package com.redsport.backend.identity.infrastructure.persistence.jpa.repositories;

import com.redsport.backend.identity.domain.model.aggregates.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
  Optional<User> lockById(@org.springframework.data.repository.query.Param("id") UUID id);

  Optional<User> findByEmail(String email);

  Optional<User> findByGoogleSubject(String googleSubject);

  boolean existsByEmail(String email);

  boolean existsByRole(com.redsport.backend.identity.domain.model.valueobjects.Roles role);
}
