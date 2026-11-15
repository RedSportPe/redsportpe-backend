package com.redsport.backend.shared.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "sequence_counters")
@Getter
public class SequenceCounter {
  @Id
  @Column(length = 80)
  private String name;

  @Column(nullable = false)
  private long counterValue;

  protected SequenceCounter() {}

  public SequenceCounter(String name) {
    this.name = name;
  }

  public long next(long floor) {
    counterValue = Math.max(counterValue, floor) + 1;
    return counterValue;
  }
}
