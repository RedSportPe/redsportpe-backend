package com.redsport.backend.shared.application;

import com.redsport.backend.shared.domain.model.DomainException;
import com.redsport.backend.shared.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SequenceService {
  private final SequenceRepository counters;

  public SequenceService(SequenceRepository counters) {
    this.counters = counters;
  }

  @Transactional
  public long next(String name, long floor) {
    return counters
        .lockByName(name)
        .orElseThrow(() -> DomainException.conflict("Falta inicializar el correlativo: " + name))
        .next(floor);
  }
}
