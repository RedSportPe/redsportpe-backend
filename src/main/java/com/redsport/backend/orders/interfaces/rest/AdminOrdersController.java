package com.redsport.backend.orders.interfaces.rest;

import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.orders.interfaces.rest.resources.OrderResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrdersController {
  private final OrderService service;

  public AdminOrdersController(OrderService service) {
    this.service = service;
  }

  @GetMapping
  public Object list() {
    return service.all().stream().map(OrderResource::from).toList();
  }

  @GetMapping("/{id}/payment")
  public Object review(Principal p, @PathVariable UUID id) {
    return service.reviewPayment(p.getName(), id);
  }

  @PostMapping("/{id}/confirm-payment")
  public Object confirm(Principal p, @PathVariable UUID id, @Valid @RequestBody Confirmation r) {
    return OrderResource.from(service.confirm(p.getName(), id, r.operationReference(), r.amount()));
  }

  @PostMapping("/{id}/advance")
  public Object advance(@PathVariable UUID id) {
    return OrderResource.from(service.advance(id));
  }

  @DeleteMapping("/{id}")
  public Object cancel(Principal p, @PathVariable UUID id) {
    return OrderResource.from(service.cancel(p.getName(), id, true));
  }

  @PostMapping("/{id}/reject-payment")
  @org.springframework.web.bind.annotation.ResponseStatus(
      org.springframework.http.HttpStatus.NO_CONTENT)
  public void reject(Principal p, @PathVariable UUID id, @Valid @RequestBody Rejection r) {
    service.rejectPayment(p.getName(), id, r.reason());
  }

  public record Rejection(@NotBlank @Size(max = 300) String reason) {}

  public record Confirmation(
      @NotBlank @Size(max = 100) String operationReference,
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {}
}
