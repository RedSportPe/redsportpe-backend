package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.application.internal.CatalogMetadataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class CatalogMetadataController {
  private final CatalogMetadataService service;

  public CatalogMetadataController(CatalogMetadataService service) {
    this.service = service;
  }

  @GetMapping("/api/catalog-metadata/colors")
  public Object colors() {
    return service.colors();
  }

  @GetMapping("/api/catalog-metadata/garment-types")
  public Object types() {
    return service.types();
  }

  @PutMapping("/api/admin/catalog-metadata/colors/{code}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object color(@PathVariable String code, @RequestBody Label r) {
    return service.color(code, r.label());
  }

  @PutMapping("/api/admin/catalog-metadata/garment-types/{code}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object type(@PathVariable String code, @RequestBody Label r) {
    return service.type(code, r.label());
  }

  public record Label(String label) {}
}
