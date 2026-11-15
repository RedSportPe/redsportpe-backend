package com.redsport.backend.identity.application.ports;

public interface GoogleIdentityTokens {
  Identity verify(String credential);

  record Identity(String subject, String email, String name) {}
}
