package com.redsport.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import com.redsport.backend.inventory.domain.model.aggregates.StockItem;
import com.redsport.backend.orders.domain.services.AmountInWords;
import com.redsport.backend.promotions.domain.model.aggregates.Promotion;
import com.redsport.backend.shared.domain.model.Money;
import com.redsport.backend.shipping.domain.services.DeliveryRules;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainRulesTest {
  @Test
  void skuEnforcesGenderSizesAndStore() {
    assertEquals("RS-CJTP-H-XL-NEG-T1", new Sku("rs-cjtp-h-xl-neg-t1").value());
    assertEquals("T12", new Sku("NK-CJTP-NA-16-NEG-T12").storeCode());
    assertThrows(IllegalArgumentException.class, () -> new Sku("RS-CJTP-H-8-NEG-T1"));
    assertThrows(IllegalArgumentException.class, () -> new Sku("RS-CJTP-NO-M-NEG-T1"));
    assertThrows(IllegalArgumentException.class, () -> new Sku("RS-CJTP-NO-5-NEG-T1"));
    assertThrows(IllegalArgumentException.class, () -> new Sku("RS-CJTP-H-M-NEG").storeCode());
  }

  @Test
  void stockNeverGoesNegativeOrBelowReservations() {
    var stock = new StockItem("RS-X-H-M-NEG-T1", 2);
    stock.reserve(2);
    assertEquals(0, stock.available());
    assertThrows(RuntimeException.class, () -> stock.reserve(1));
    assertThrows(RuntimeException.class, () -> stock.adjust(1));
    stock.release(1);
    stock.commit(1);
    assertEquals(1, stock.available());
    assertThrows(RuntimeException.class, () -> stock.release(1));
  }

  @Test
  void promotionExpiryIncludesTheWholeLimaDateAndCountsReservations() {
    var day = LocalDate.of(2026, 11, 15);
    var promo = new Promotion(UUID.randomUUID(), new BigDecimal("10.50"), day, 2);
    assertTrue(promo.active(day));
    promo.reserve(2);
    assertFalse(promo.active(day));
    promo.release(1);
    assertTrue(promo.active(day));
    promo.commit(1);
    assertEquals(1, promo.getUnitsSold());
    assertFalse(promo.active(day.plusDays(1)));
  }

  @Test
  void threePmCutoffUsesPeruTime() {
    var before = Instant.parse("2026-11-15T19:59:59Z");
    var after = Instant.parse("2026-11-15T20:00:00Z");
    assertEquals(LocalDate.of(2026, 11, 16), DeliveryRules.deliveryDate("motorizado", before));
    assertEquals(LocalDate.of(2026, 11, 17), DeliveryRules.deliveryDate("motorizado", after));
    assertEquals(LocalDate.of(2026, 11, 15), DeliveryRules.deliveryDate("shalom", before));
    assertEquals(LocalDate.of(2026, 11, 16), DeliveryRules.deliveryDate("shalom", after));
  }

  @Test
  void amountsKeepCentimosAndRejectExtraDecimals() {
    assertEquals("CIEN CON 99/100 SOLES", AmountInWords.soles(new BigDecimal("100.99")));
    assertEquals("VEINTIÚN CON 01/100 SOLES", AmountInWords.soles(new BigDecimal("21.01")));
    assertThrows(IllegalArgumentException.class, () -> Money.amount(new BigDecimal("1.001")));
    assertThrows(IllegalArgumentException.class, () -> Money.amount(new BigDecimal("-1")));
  }
}
