package com.redsport.backend.orders.interfaces.rest;

import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.orders.interfaces.rest.resources.OrderResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operator/sales")
@PreAuthorize("hasRole('OPERATOR')")
public class PosController {
  private final OrderService service;

  public PosController(OrderService service) {
    this.service = service;
  }

  @GetMapping("/{id}/receipt")
  public Object receipt(Principal p, @PathVariable java.util.UUID id) {
    return service.receipt(p.getName(), id);
  }

  @GetMapping
  public Object list(
      Principal p,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(required = false) String query) {
    return service.sales(p.getName(), from, to, query).stream().map(OrderResource::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Object sale(
      Principal p, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody Sale r) {
    return OrderResource.from(
        service.pos(
            p.getName(),
            key,
            new OrderService.PosSale(
                r.customerName(),
                r.customerDoc(),
                r.paymentMethod(),
                r.cashReceived(),
                r.qrAmount(),
                r.total(),
                r.discountReason(),
                r.items().stream()
                    .map(i -> new OrderService.ItemRequest(i.sku(), i.quantity(), i.unitPrice()))
                    .toList())));
  }

  public record Line(
      @NotBlank String sku,
      @Min(1) @Max(9999) int quantity,
      @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice) {}

  public record Sale(
      @Size(max = 150) String customerName,
      @Size(max = 20) String customerDoc,
      @NotBlank String paymentMethod,
      @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal cashReceived,
      @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal qrAmount,
      @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal total,
      @Size(max = 300) String discountReason,
      @NotEmpty @Size(max = 100) List<@Valid Line> items) {}
}
