package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.application.internal.FavoritesService;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {
  private final FavoritesService service;

  public FavoritesController(FavoritesService service) {
    this.service = service;
  }

  @GetMapping
  public Object list(Principal p) {
    return service.list(p.getName());
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void add(Principal p, @PathVariable UUID id) {
    service.set(p.getName(), id, true);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(Principal p, @PathVariable UUID id) {
    service.set(p.getName(), id, false);
  }
}
