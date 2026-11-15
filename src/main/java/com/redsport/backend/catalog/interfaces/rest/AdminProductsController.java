package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.application.internal.CatalogService;
import com.redsport.backend.catalog.interfaces.rest.resources.SaveProductResource;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductsController {
  private final CatalogService service;

  public AdminProductsController(CatalogService service) {
    this.service = service;
  }

  @GetMapping
  public Object list() {
    return service.list(true);
  }

  @GetMapping("/{id}")
  public Object get(@PathVariable UUID id) {
    return service.get(id, true);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Object create(@Valid @RequestBody SaveProductResource r) {
    return service.save(null, r.toCommand());
  }

  @PutMapping("/{id}")
  public Object update(@PathVariable UUID id, @Valid @RequestBody SaveProductResource r) {
    return service.save(id, r.toCommand());
  }

  @PatchMapping("/{id}/publication")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void publish(@PathVariable UUID id, @RequestBody Publication r) {
    service.publish(id, r.published());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  public record Publication(boolean published) {}
}
