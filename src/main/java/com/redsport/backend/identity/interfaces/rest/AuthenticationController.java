package com.redsport.backend.identity.interfaces.rest;

import com.redsport.backend.identity.domain.model.commands.SignInCommand;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.interfaces.rest.resources.AuthenticatedUserResource;
import com.redsport.backend.identity.interfaces.rest.resources.SignInResource;
import com.redsport.backend.identity.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
public class AuthenticationController {

  private final UserCommandService userCommandService;
  private final com.redsport.backend.stores.application.ports.StoreAccess stores;

  public AuthenticationController(
      UserCommandService userCommandService,
      com.redsport.backend.stores.application.ports.StoreAccess stores) {
    this.userCommandService = userCommandService;
    this.stores = stores;
  }

  @PostMapping("/sign-up")
  public ResponseEntity<com.redsport.backend.identity.interfaces.rest.resources.UserResource>
      signUp(@jakarta.validation.Valid @RequestBody RegisterResource resource) {
    var user =
        userCommandService.handle(
            new com.redsport.backend.identity.domain.model.commands.CreateUserCommand(
                resource.name(),
                resource.email(),
                resource.password(),
                com.redsport.backend.identity.domain.model.valueobjects.Roles.customer));
    return ResponseEntity.status(201)
        .body(
            com.redsport.backend.identity.interfaces.rest.transform.UserResourceFromEntityAssembler
                .toResourceFromEntity(user));
  }

  public record RegisterResource(
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 120)
          String name,
      @jakarta.validation.constraints.NotBlank
          @jakarta.validation.constraints.Email
          @jakarta.validation.constraints.Size(max = 160)
          String email,
      @jakarta.validation.constraints.NotBlank
          @jakarta.validation.constraints.Size(min = 8, max = 72)
          String password) {}

  @PostMapping("/sign-in")
  public ResponseEntity<AuthenticatedUserResource> signIn(
      @jakarta.validation.Valid @RequestBody SignInResource resource) {
    var command = new SignInCommand(resource.email(), resource.password());
    var result = userCommandService.handle(command);

    if (result.isEmpty()) {
      return ResponseEntity.status(401).build();
    }

    var resourceResponse =
        AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
            result.get().user(),
            result.get().token(),
            stores.assignedStore(result.get().user().getEmail()).map(s -> s.code()).orElse(null));
    return ResponseEntity.ok(resourceResponse);
  }
}
