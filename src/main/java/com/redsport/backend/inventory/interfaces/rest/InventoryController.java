package com.redsport.backend.inventory.interfaces.rest;

import com.redsport.backend.inventory.application.internal.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {
  private final InventoryService service;

  public InventoryController(InventoryService service) {
    this.service = service;
  }

  @GetMapping
  public Object list() {
    return service.list();
  }

  @PatchMapping
  public Object adjust(@Valid @RequestBody Batch r) {
    return service.adjust(r.items());
  }

  public record Batch(@NotEmpty @Size(max = 500) @Valid List<InventoryService.Adjustment> items) {}
}
