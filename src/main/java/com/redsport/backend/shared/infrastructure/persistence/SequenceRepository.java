package com.redsport.backend.shared.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SequenceRepository extends JpaRepository<SequenceCounter, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from SequenceCounter s where s.name=:name")
  Optional<SequenceCounter> lockByName(@Param("name") String name);
}
