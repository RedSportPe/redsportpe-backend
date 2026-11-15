package com.redsport.backend.identity.interfaces.rest;

import com.redsport.backend.identity.application.internal.GoogleAuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthenticationController {
  private final GoogleAuthenticationService service;

  public GoogleAuthenticationController(GoogleAuthenticationService service) {
    this.service = service;
  }

  @PostMapping
  public Object authenticate(@Valid @RequestBody Credential r) {
    var result = service.authenticate(r.credential());
    return com.redsport.backend.identity.interfaces.rest.transform
        .AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
        result.user(), result.token());
  }

  public record Credential(@NotBlank @Size(max = 10000) String credential) {}
}
