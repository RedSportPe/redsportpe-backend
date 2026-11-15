package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.application.internal.CatalogService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductsController {
  private final CatalogService service;

  public ProductsController(CatalogService service) {
    this.service = service;
  }

  @GetMapping
  public Object list() {
    return service.list(false);
  }

  @GetMapping("/{id}")
  public Object get(@PathVariable UUID id) {
    return service.get(id, false);
  }

  @GetMapping("/by-sku/{sku}")
  public Object sku(@PathVariable String sku) {
    return service.lookup(sku);
  }
}
