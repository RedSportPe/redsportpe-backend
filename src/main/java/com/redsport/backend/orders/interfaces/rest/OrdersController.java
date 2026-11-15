package com.redsport.backend.orders.interfaces.rest;

import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.orders.interfaces.rest.resources.OrderResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrdersController {
  private final OrderService service;

  public OrdersController(OrderService service) {
    this.service = service;
  }

  @GetMapping
  public Object list(Principal p) {
    return service.mine(p.getName()).stream().map(OrderResource::from).toList();
  }

  @GetMapping("/{id}")
  public Object get(Principal p, @PathVariable UUID id) {
    return OrderResource.from(service.get(p.getName(), id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Object checkout(
      Principal p, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody Checkout r) {
    return OrderResource.from(
        service.checkout(
            p.getName(),
            key,
            new OrderService.Checkout(
                r.method(),
                r.fullName(),
                r.phone(),
                r.address(),
                r.district(),
                r.agency(),
                r.items().stream()
                    .map(i -> new OrderService.ItemRequest(i.sku(), i.quantity(), null))
                    .toList())));
  }

  @PostMapping("/{id}/payment-submission")
  public Object submit(Principal p, @PathVariable UUID id, @Valid @RequestBody Reference r) {
    return OrderResource.from(service.submit(p.getName(), id, r.operationReference()));
  }

  @GetMapping("/{id}/payment")
  public Object payment(Principal p, @PathVariable UUID id) {
    return service.payment(p.getName(), id);
  }

  @PostMapping("/{id}/renew-qr")
  public Object renew(Principal p, @PathVariable UUID id) {
    return OrderResource.from(service.renew(p.getName(), id));
  }

  @DeleteMapping("/{id}")
  public Object cancel(Principal p, @PathVariable UUID id) {
    return OrderResource.from(service.cancel(p.getName(), id, false));
  }

  public record Reference(@NotBlank @Size(max = 100) String operationReference) {}

  public record Line(@NotBlank String sku, @Min(1) @Max(9999) int quantity) {}

  public record Checkout(
      @NotBlank String method,
      @NotBlank @Size(max = 150) String fullName,
      @NotBlank @Size(max = 20) String phone,
      @Size(max = 200) String address,
      @Size(max = 100) String district,
      @Size(max = 200) String agency,
      @NotEmpty @Size(max = 100) List<@Valid Line> items) {}
}
