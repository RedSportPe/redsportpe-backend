package com.redsport.backend.promotions.interfaces.rest;

import com.redsport.backend.promotions.application.internal.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PromotionsController {
  private final PromotionService service;

  public PromotionsController(PromotionService service) {
    this.service = service;
  }

  @GetMapping("/api/promotions")
  public Object list() {
    return service.list(false);
  }

  @GetMapping("/api/admin/promotions")
  @PreAuthorize("hasRole('ADMIN')")
  public Object admin() {
    return service.list(true);
  }

  @PostMapping("/api/admin/promotions")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public Object create(@Valid @RequestBody SavePromotion r) {
    return service.create(r.productId(), r.discountAmount(), r.endsAt(), r.maxUnits());
  }

  @DeleteMapping("/api/admin/promotions/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancel(@PathVariable UUID id) {
    service.cancel(id);
  }

  public record SavePromotion(
      @NotNull UUID productId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal discountAmount,
      LocalDate endsAt,
      @Min(1) Integer maxUnits) {}
}
