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

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var command = new SignInCommand(resource.email(), resource.password());
        var result = userCommandService.handle(command);

        if (result.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        var resourceResponse = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(result.get().user(), result.get().token());
        return ResponseEntity.ok(resourceResponse);
    }
}