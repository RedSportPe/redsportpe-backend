package com.redsport.backend.identity.infrastructure.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.redsport.backend.identity.application.ports.GoogleIdentityTokens;
import com.redsport.backend.shared.domain.model.DomainException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifier implements GoogleIdentityTokens {
  private final String clientId;
  private volatile GoogleIdTokenVerifier verifier;

  public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
    this.clientId = clientId;
  }

  private synchronized GoogleIdTokenVerifier verifier()
      throws java.security.GeneralSecurityException, java.io.IOException {
    if (verifier == null)
      verifier =
          new GoogleIdTokenVerifier.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
              .setAudience(List.of(clientId))
              .build();
    return verifier;
  }

  public Identity verify(String credential) {
    if (clientId.isBlank())
      throw new DomainException(503, "Configura GOOGLE_CLIENT_ID para habilitar Google");
    try {
      var token = verifier().verify(credential);
      if (token == null || !Boolean.TRUE.equals(token.getPayload().getEmailVerified()))
        throw new DomainException(401, "Credencial de Google inválida");
      var payload = token.getPayload();
      return new Identity(
          payload.getSubject(),
          payload.getEmail().toLowerCase(Locale.ROOT),
          Objects.toString(payload.get("name"), payload.getEmail()));
    } catch (DomainException e) {
      throw e;
    } catch (IllegalArgumentException e) {
      throw new DomainException(401, "Credencial de Google inválida");
    } catch (java.security.GeneralSecurityException | java.io.IOException e) {
      throw new DomainException(503, "No se pudo verificar Google. Inténtalo más tarde");
    }
  }
}
