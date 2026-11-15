package com.redsport.backend.promotions.application.internal;

import com.redsport.backend.catalog.application.ports.Catalog;
import com.redsport.backend.promotions.application.ports.Promotions;
import com.redsport.backend.promotions.domain.model.aggregates.Promotion;
import com.redsport.backend.promotions.infrastructure.persistence.jpa.repositories.PromotionRepository;
import com.redsport.backend.shared.domain.model.DomainException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromotionService implements Promotions {
  private final PromotionRepository promotions;
  private final Catalog catalog;
  private final Clock clock;

  public PromotionService(PromotionRepository promotions, Catalog catalog, Clock clock) {
    this.promotions = promotions;
    this.catalog = catalog;
    this.clock = clock;
  }

  private LocalDate today() {
    return LocalDate.now(clock.withZone(ZoneId.of("America/Lima")));
  }

  @Transactional(readOnly = true)
  public List<Promotion> list(boolean admin) {
    return promotions.findAll().stream().filter(p -> admin || p.active(today())).toList();
  }

  public Promotion create(UUID productId, BigDecimal discount, LocalDate endsAt, Integer maxUnits) {
    var price = catalog.lockPrice(productId);
    if (discount == null || discount.compareTo(price) >= 0)
      throw new IllegalArgumentException("El descuento debe ser menor al precio");
    if (endsAt != null && endsAt.isBefore(today()))
      throw new IllegalArgumentException("La promoción ya venció");
    if (promotions.lockByProduct(productId).stream().anyMatch(p -> p.active(today())))
      throw DomainException.conflict("Ya existe una promoción activa");
    return promotions.save(new Promotion(productId, discount, endsAt, maxUnits));
  }

  public void cancel(UUID id) {
    var found =
        promotions
            .findById(id)
            .orElseThrow(() -> DomainException.missing("Promoción no encontrada"));
    catalog.lockPrice(found.getProductId());
    promotions.lock(id).orElseThrow().cancel();
  }

  public Quote reserve(UUID productId, BigDecimal price, int quantity) {
    var active =
        promotions.lockByProduct(productId).stream().filter(p -> p.active(today())).findFirst();
    if (active.isEmpty()) return new Quote(price, null);
    var p = active.get();
    if (p.getDiscountAmount().compareTo(price) >= 0)
      throw DomainException.conflict("Actualiza la promoción tras el cambio de precio");
    p.reserve(quantity);
    return new Quote(price.subtract(p.getDiscountAmount()), p.getId());
  }

  public void release(Map<UUID, Integer> q) {
    each(q, (p, n) -> p.release(n));
  }

  public void commit(Map<UUID, Integer> q) {
    each(q, (p, n) -> p.commit(n));
  }

  private void each(
      Map<UUID, Integer> q, java.util.function.BiConsumer<Promotion, Integer> action) {
    q.keySet().stream()
        .sorted()
        .forEach(id -> action.accept(promotions.lock(id).orElseThrow(), q.get(id)));
  }
}
