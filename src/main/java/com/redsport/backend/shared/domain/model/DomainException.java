package com.redsport.backend.shared.domain.model;

public class DomainException extends RuntimeException {
  private final int status;

  public DomainException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int status() {
    return status;
  }

  public static DomainException missing(String message) {
    return new DomainException(404, message);
  }

  public static DomainException conflict(String message) {
    return new DomainException(409, message);
  }

  public static DomainException forbidden() {
    return new DomainException(403, "No tienes permiso para esta operación");
  }
}
