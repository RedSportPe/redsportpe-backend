package com.redsport.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.redsport.backend.identity.application.internal.GoogleAuthenticationService;
import com.redsport.backend.identity.application.ports.GoogleIdentityTokens;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import com.redsport.backend.shared.domain.model.DomainException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GoogleAuthenticationTest {
  @Test
  void verifiedGoogleIdentityCreatesACustomer() {
    var users = mock(UserRepository.class);
    var tokens = mock(TokenService.class);
    var verifier = mock(GoogleIdentityTokens.class);
    when(verifier.verify("credential"))
        .thenReturn(new GoogleIdentityTokens.Identity("subject", "new@example.test", "New User"));
    when(users.findByGoogleSubject("subject")).thenReturn(Optional.empty());
    when(users.save(any())).thenAnswer(i -> i.getArgument(0));
    when(tokens.generateToken("new@example.test", "customer")).thenReturn("jwt");
    var result =
        new GoogleAuthenticationService(users, tokens, verifier).authenticate("credential");
    assertEquals("customer", result.user().getRole().name());
    assertEquals("google", result.user().getProvider().name());
    assertEquals("subject", result.user().getGoogleSubject());
    assertEquals("jwt", result.token());
    assertNull(result.user().getPasswordHash());
  }

  @Test
  void googleCannotTakeOverAnExistingEmailAccount() {
    var users = mock(UserRepository.class);
    var verifier = mock(GoogleIdentityTokens.class);
    when(verifier.verify("credential"))
        .thenReturn(new GoogleIdentityTokens.Identity("subject", "admin@example.test", "Name"));
    when(users.findByGoogleSubject("subject")).thenReturn(Optional.empty());
    when(users.existsByEmail("admin@example.test")).thenReturn(true);
    var error =
        assertThrows(
            DomainException.class,
            () ->
                new GoogleAuthenticationService(users, mock(TokenService.class), verifier)
                    .authenticate("credential"));
    assertEquals(409, error.status());
    verify(users, never()).save(any());
  }

  @Test
  void invalidGoogleCredentialCannotCreateAnAccount() {
    var users = mock(UserRepository.class);
    var verifier = mock(GoogleIdentityTokens.class);
    when(verifier.verify("invalid")).thenThrow(new DomainException(401, "Invalid token"));
    assertThrows(
        DomainException.class,
        () ->
            new GoogleAuthenticationService(users, mock(TokenService.class), verifier)
                .authenticate("invalid"));
    verifyNoInteractions(users);
  }
}
