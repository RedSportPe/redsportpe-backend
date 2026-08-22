package com.redsport.backend.identity.interfaces.rest;

import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.interfaces.rest.resources.CreateUserResource;
import com.redsport.backend.identity.interfaces.rest.resources.UserResource;
import com.redsport.backend.identity.interfaces.rest.transform.UserResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
public class UsersController {

    private final UserCommandService userCommandService;

    public UsersController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(@RequestBody CreateUserResource resource) {
        var command = new CreateUserCommand(
                resource.name(),
                resource.email(),
                resource.password(),
                Roles.valueOf(resource.role())   // "operator", "admin", "customer"
        );
        var user = userCommandService.handle(command);
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
        return ResponseEntity.status(201).body(userResource);
    }
}