package com.redsport.backend.orders.interfaces.rest;

import com.redsport.backend.orders.application.internal.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private final CartService service;

  public CartController(CartService service) {
    this.service = service;
  }

  @GetMapping
  public Object get(Principal p) {
    return service.get(p.getName());
  }

  @PutMapping
  public Object save(Principal p, @Valid @RequestBody Cart r) {
    return service.save(p.getName(), r.items());
  }

  @DeleteMapping
  public Object clear(Principal p) {
    return service.save(p.getName(), List.of());
  }

  public record Cart(@NotNull @Size(max = 100) @Valid List<CartService.Line> items) {}
}
