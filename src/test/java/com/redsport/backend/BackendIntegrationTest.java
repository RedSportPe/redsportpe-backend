package com.redsport.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import com.redsport.backend.catalog.application.internal.CatalogService;
import com.redsport.backend.catalog.interfaces.rest.resources.SaveProductResource;
import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import com.redsport.backend.inventory.application.internal.InventoryService;
import com.redsport.backend.inventory.application.ports.Inventory;
import com.redsport.backend.orders.application.internal.OrderService;
import com.redsport.backend.promotions.application.internal.PromotionService;
import com.redsport.backend.shared.domain.model.DomainException;
import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.commands.*;
import com.redsport.backend.stores.domain.services.StoreCommandService;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;

@EnabledIfEnvironmentVariable(
    named = "TEST_DATABASE_URL",
    matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/redsport_test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "jwt.secret=only-for-isolated-tests-012345678901234567890123456789",
      "app.seed-admin.enabled=false",
      "app.order-expiration.enabled=false",
      "spring.jpa.hibernate.ddl-auto=validate"
    })
@Import(BackendIntegrationTest.TestTime.class)
class BackendIntegrationTest {
  @DynamicPropertySource
  static void database(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", () -> System.getenv("TEST_DATABASE_URL"));
    r.add("spring.datasource.username", () -> System.getenv("TEST_DATABASE_USERNAME"));
    r.add(
        "spring.datasource.password",
        () -> Objects.toString(System.getenv("TEST_DATABASE_PASSWORD"), ""));
  }

  @TestConfiguration
  static class TestTime {
    @Bean
    @Primary
    MutableClock testClock() {
      return new MutableClock();
    }
  }

  static class MutableClock extends Clock {
    final AtomicReference<Instant> now =
        new AtomicReference<>(Instant.parse("2026-11-15T19:00:00Z"));

    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    public Clock withZone(ZoneId zone) {
      return Clock.fixed(instant(), zone);
    }

    public Instant instant() {
      return now.get();
    }
  }

  @Autowired MutableClock clock;
  @Autowired UserCommandService users;
  @Autowired CustomerAccounts accounts;
  @Autowired CatalogService catalog;
  @Autowired Inventory inventory;
  @Autowired InventoryService stocks;
  @Autowired OrderService orders;
  @Autowired PromotionService promotions;
  @Autowired StoreCommandService stores;
  @Autowired TokenService tokens;
  @Autowired com.redsport.backend.catalog.application.internal.FavoritesService favorites;
  @Autowired com.redsport.backend.orders.application.internal.CartService carts;
  @Autowired com.redsport.backend.identity.application.internal.ProfileService profiles;

  @Value("${local.server.port}")
  int port;

  String admin, customer, operator;
  Store store;
  final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  static BigDecimal money(String value) {
    return new BigDecimal(value);
  }

  static String key() {
    return UUID.randomUUID().toString();
  }

  String user(Roles role) {
    String email = key() + "@example.test";
    users.handle(new CreateUserCommand("Test User", email, "test-pass-123", role));
    return email;
  }

  @BeforeEach
  void setup() {
    clock.now.set(Instant.parse("2026-11-15T19:00:00Z"));
    admin = user(Roles.admin);
    customer = user(Roles.customer);
    operator = key() + "@example.test";
    store =
        stores.handle(
            new CreateStoreWithOperatorCommand(
                "Test Store",
                "Test Address",
                "Test Cashier",
                "Lima",
                "Lima",
                "Lima",
                "999999999",
                operator,
                "operator-pass-123"));
  }

  record Product(UUID id, String sku) {}

  Product product(int quantity) {
    String code = "P" + key().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    String sku = "RS-" + code + "-H-M-NEG-" + store.getCode();
    var r =
        catalog.save(
            null,
            new SaveProductResource(
                    code,
                    "Test Product",
                    "Description",
                    "Conjuntos",
                    money("100.99"),
                    true,
                    false,
                    List.of(new SaveProductResource.VariantData(sku, "H", "M", "NEG", quantity)),
                    List.of())
                .toCommand());
    return new Product(UUID.fromString(r.id()), sku);
  }

  OrderService.Checkout checkout(Product p, int quantity) {
    return new OrderService.Checkout(
        "motorizado",
        "Test Customer",
        "999999999",
        "Street 123",
        "Lima",
        null,
        List.of(new OrderService.ItemRequest(p.sku, quantity, null)));
  }

  HttpResponse<String> request(String method, String url, String email, String body)
      throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + url))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json");
    if (email != null)
      builder.header(
          "Authorization", "Bearer " + tokens.generateToken(email, accounts.current(email).role()));
    return http.send(
        builder
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void routesEnforceAuthenticationRolesAndSwaggerCompatibility() throws Exception {
    assertEquals(200, request("GET", "/api/products", null, null).statusCode());
    assertEquals(200, request("GET", "/v3/api-docs", null, null).statusCode());
    assertEquals(401, request("GET", "/api/admin/products", null, null).statusCode());
    assertEquals(403, request("GET", "/api/admin/products", customer, null).statusCode());
    assertEquals(200, request("GET", "/api/admin/products", admin, null).statusCode());
    assertEquals(403, request("GET", "/api/operator/sales", customer, null).statusCode());
    assertEquals(
        401,
        request(
                "POST",
                "/api/auth/sign-in",
                null,
                "{\"email\":\"unknown@example.test\",\"password\":\"wrong-password\"}")
            .statusCode());
    assertEquals(
        503,
        request("POST", "/api/auth/google", null, "{\"credential\":\"invalid\"}").statusCode());
  }

  @Test
  void publicRegistrationCannotChooseAnAdministrativeRole() throws Exception {
    String email = key() + "@example.test";
    var response =
        request(
            "POST",
            "/api/auth/sign-up",
            null,
            "{\"name\":\"New User\",\"email\":\""
                + email
                + "\",\"password\":\"password-123\",\"role\":\"admin\"}");
    assertEquals(201, response.statusCode(), response.body());
    assertEquals("customer", accounts.current(email).role());
    assertEquals(
        400,
        request(
                "POST",
                "/api/auth/sign-up",
                null,
                "{\"name\":\"x\",\"email\":\"invalid\",\"password\":\"a\"}")
            .statusCode());
  }

  @Test
  void paymentIsAtomicAndIdempotentWithDecimalPointsAndPromotionCaps() {
    var p = product(3);
    promotions.create(p.id, money("20.00"), LocalDate.of(2026, 11, 15), 2);
    String key = key();
    var o = orders.checkout(customer, key, checkout(p, 2));
    assertEquals(money("161.98"), o.getTotal());
    assertEquals(1, inventory.available(p.sku));
    assertEquals(o.getId(), orders.checkout(customer, key, checkout(p, 2)).getId());
    assertThrows(
        DomainException.class,
        () -> orders.confirm(admin, o.getId(), "OP" + key.replace("-", ""), money("1.00")));
    String operation = "OP" + key.replace("-", "");
    orders.submit(customer, o.getId(), operation);
    orders.confirm(admin, o.getId(), operation, money("161.98"));
    orders.confirm(admin, o.getId(), operation, money("161.98"));
    assertEquals(money("161.98"), accounts.current(customer).points());
    assertEquals(1, inventory.available(p.sku));
    assertEquals(2, catalog.get(p.id, true).salesCount());
    orders.cancel(admin, o.getId(), true);
    assertEquals(money("161.98"), accounts.current(customer).points());
    assertEquals(money("100.99"), orders.checkout(customer, key(), checkout(p, 1)).getTotal());
  }

  @Test
  void customerCannotReadOrPaySomeoneElsesOrder() throws Exception {
    var p = product(2);
    var o = orders.checkout(customer, key(), checkout(p, 1));
    String other = user(Roles.customer);
    assertEquals(404, request("GET", "/api/orders/" + o.getId(), other, null).statusCode());
    assertEquals(
        403,
        request(
                "POST",
                "/api/admin/orders/" + o.getId() + "/confirm-payment",
                customer,
                "{\"operationReference\":\"ABC123\",\"amount\":100.99}")
            .statusCode());
    assertThrows(DomainException.class, () -> catalog.delete(p.id));
    orders.cancel(customer, o.getId(), false);
    assertEquals(2, inventory.available(p.sku));
    catalog.delete(p.id);
    assertThrows(DomainException.class, () -> catalog.get(p.id, false));
  }

  @Test
  void expiryReleasesReservationsAndRenewalRequotesCurrentPrice() {
    var p = product(2);
    promotions.create(p.id, money("10.00"), LocalDate.of(2026, 11, 15), 2);
    var o = orders.checkout(customer, key(), checkout(p, 1));
    assertEquals(1, inventory.available(p.sku));
    clock.now.set(clock.instant().plus(Duration.ofHours(10)));
    assertTrue(orders.expire(o.getId()));
    assertFalse(orders.expire(o.getId()));
    assertEquals(2, inventory.available(p.sku));
    var renewed = orders.renew(customer, o.getId());
    assertEquals(money("100.99"), renewed.getTotal());
    assertEquals(1, inventory.available(p.sku));
    assertEquals(clock.instant().plus(Duration.ofHours(8)), renewed.getQrExpiresAt());
  }

  @Test
  void concurrentCheckoutCannotSellTheLastUnitTwice() throws Exception {
    var p = product(1);
    String other = user(Roles.customer);
    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var attempts =
          List.of(customer, other).stream()
              .map(
                  email ->
                      pool.submit(
                          () -> {
                            ready.countDown();
                            start.await();
                            try {
                              orders.checkout(email, key(), checkout(p, 1));
                              return true;
                            } catch (DomainException e) {
                              assertEquals(409, e.status());
                              return false;
                            }
                          }))
              .toList();
      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();
      int successful = 0;
      for (var f : attempts) if (f.get(20, TimeUnit.SECONDS)) successful++;
      assertEquals(1, successful);
      assertEquals(0, inventory.available(p.sku));
    }
  }

  @Test
  void posValidatesMixedPaymentAndKeepsReceiptAndPointsCorrect() {
    var p = product(5);
    String key = key();
    var sale =
        new OrderService.PosSale(
            "Customer",
            "12345678",
            "mixto",
            money("40"),
            money("100"),
            money("140"),
            "Negociación autorizada",
            List.of(new OrderService.ItemRequest(p.sku, 2, money("75"))));
    var o = orders.pos(operator, key, sale);
    assertEquals("DELIVERED", o.getStatus());
    assertEquals(3, o.getTrackingStep());
    assertTrue(o.getBoletaNumber().startsWith(store.getBoletaSeries() + "-"));
    assertEquals(3, inventory.available(p.sku));
    assertEquals(0, accounts.current(operator).points().signum());
    assertEquals(o.getId(), orders.pos(operator, key, sale).getId());
    assertNotNull(orders.receipt(operator, o.getId()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            orders.pos(
                operator,
                key(),
                new OrderService.PosSale(
                    null,
                    null,
                    "efectivo",
                    money("1"),
                    null,
                    null,
                    null,
                    List.of(new OrderService.ItemRequest(p.sku, 1, null)))));
  }

  @Test
  void invalidPosPaymentRollsBackStockPromotionAndReceiptCounter() {
    var p = product(2);
    var promo = promotions.create(p.id, money("10"), null, 2);
    var request =
        new OrderService.PosSale(
            null,
            null,
            "efectivo",
            money("1"),
            null,
            null,
            "Promo",
            List.of(new OrderService.ItemRequest(p.sku, 1, null)));
    assertThrows(IllegalArgumentException.class, () -> orders.pos(operator, key(), request));
    assertEquals(2, inventory.available(p.sku));
    var valid =
        new OrderService.PosSale(
            null,
            null,
            "efectivo",
            money("200"),
            null,
            null,
            "Promo",
            List.of(new OrderService.ItemRequest(p.sku, 1, null)));
    assertTrue(orders.pos(operator, key(), valid).getBoletaNumber().endsWith("00000001"));
  }

  @Test
  void operatorCannotSellAnotherStoresSkuOrUseDeactivatedStore() throws Exception {
    var p = product(1);
    String other = key() + "@example.test";
    stores.handle(
        new CreateStoreWithOperatorCommand(
            "Other",
            "Address",
            "Cashier",
            "Lima",
            "Lima",
            "Lima",
            "999",
            other,
            "operator-pass-123"));
    var request =
        new OrderService.PosSale(
            null,
            null,
            "qr",
            null,
            null,
            null,
            null,
            List.of(new OrderService.ItemRequest(p.sku, 1, null)));
    assertThrows(DomainException.class, () -> orders.pos(other, key(), request));
    assertEquals(1, inventory.available(p.sku));
    stores.handle(new DeleteStoreCommand(store.getId()));
    assertEquals(401, request("GET", "/api/operator/sales", operator, null).statusCode());
  }

  @Test
  void stockDraftDetectsAConcurrentChange() {
    var p = product(2);
    var state = stocks.list().stream().filter(s -> s.sku().equals(p.sku)).findFirst().orElseThrow();
    stocks.adjust(List.of(new InventoryService.Adjustment(p.sku, 3, state.version())));
    assertThrows(
        DomainException.class,
        () -> stocks.adjust(List.of(new InventoryService.Adjustment(p.sku, 4, state.version()))));
    assertEquals(3, inventory.available(p.sku));
  }

  @Test
  void trackingRequiresPaymentAndStopsAtDelivered() {
    var p = product(1);
    var o = orders.checkout(customer, key(), checkout(p, 1));
    assertThrows(DomainException.class, () -> orders.advance(o.getId()));
    orders.confirm(admin, o.getId(), key(), o.getTotal());
    orders.advance(o.getId());
    orders.advance(o.getId());
    assertEquals("DELIVERED", orders.advance(o.getId()).getStatus());
    assertThrows(DomainException.class, () -> orders.advance(o.getId()));
  }

  @Test
  void sameOperationCannotPayTwoOrders() {
    var p = product(3);
    var first = orders.checkout(customer, key(), checkout(p, 1));
    var second = orders.checkout(customer, key(), checkout(p, 1));
    String ref = key();
    orders.confirm(admin, first.getId(), ref, first.getTotal());
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () -> orders.confirm(admin, second.getId(), ref, second.getTotal()));
    assertNull(orders.get(customer, second.getId()).getPaidAt());
    assertEquals(first.getTotal(), accounts.current(customer).points());
  }

  @Test
  void favoritesCartAndFirstDeliveryPersistPerCustomer() {
    var p = product(3);
    String other = user(Roles.customer);
    favorites.set(customer, p.id, true);
    favorites.set(customer, p.id, true);
    assertEquals(List.of(p.id), favorites.list(customer));
    assertTrue(favorites.list(other).isEmpty());
    carts.save(
        customer,
        List.of(new com.redsport.backend.orders.application.internal.CartService.Line(p.sku, 2)));
    assertEquals(1, carts.get(customer).size());
    assertTrue(carts.get(other).isEmpty());
    assertEquals(3, inventory.available(p.sku));
    orders.checkout(customer, key(), checkout(p, 1));
    assertTrue(carts.get(customer).isEmpty());
    assertEquals("Street 123", profiles.get(customer).getAddress());
    profiles.save(customer, "999", "New Address", "Lima", null);
    orders.checkout(customer, key(), checkout(p, 1));
    assertEquals("New Address", profiles.get(customer).getAddress());
    favorites.set(customer, p.id, false);
    assertTrue(favorites.list(customer).isEmpty());
  }

  @Test
  void automaticPromotionDoesNotRequireANegotiationReason() {
    var p = product(2);
    promotions.create(p.id, money("10"), null, 2);
    var sale =
        orders.pos(
            operator,
            key(),
            new OrderService.PosSale(
                null,
                null,
                "qr",
                null,
                null,
                null,
                null,
                List.of(new OrderService.ItemRequest(p.sku, 1, null))));
    assertEquals(money("90.99"), sale.getTotal());
    assertNull(sale.getDiscountReason());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            orders.pos(
                operator,
                key(),
                new OrderService.PosSale(
                    null,
                    null,
                    "qr",
                    null,
                    null,
                    null,
                    null,
                    List.of(new OrderService.ItemRequest(p.sku, 1, money("80"))))));
  }

  @Test
  void rejectedPaymentCanBeCorrectedAndStoreCredentialsCanBeUpdated() {
    var p = product(1);
    var order = orders.checkout(customer, key(), checkout(p, 1));
    orders.submit(customer, order.getId(), "MISTAKE123");
    orders.rejectPayment(admin, order.getId(), "Operación no encontrada");
    orders.submit(customer, order.getId(), "CORRECT123");
    orders.confirm(admin, order.getId(), "CORRECT123", order.getTotal());
    stores.handle(
        new UpdateStoreCommand(
            store.getId(),
            "Renamed Store",
            "New Address",
            "New Cashier",
            "Lima",
            "Lima",
            "Lima",
            "999"));
    stores.handle(new UpdateOperatorCommand(store.getId(), "New Cashier", operator, ""));
    var login =
        users
            .handle(
                new com.redsport.backend.identity.domain.model.commands.SignInCommand(
                    operator, "operator-pass-123"))
            .orElseThrow();
    assertEquals("New Cashier", login.user().getName());
  }
}
