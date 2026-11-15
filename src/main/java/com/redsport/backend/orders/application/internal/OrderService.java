package com.redsport.backend.orders.application.internal;

import com.redsport.backend.catalog.application.ports.Catalog;
import com.redsport.backend.catalog.domain.model.valueobjects.Sku;
import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.inventory.application.ports.Inventory;
import com.redsport.backend.orders.domain.model.aggregates.SalesOrder;
import com.redsport.backend.orders.domain.model.entities.OrderLine;
import com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.redsport.backend.payments.application.ports.Payments;
import com.redsport.backend.promotions.application.ports.Promotions;
import com.redsport.backend.shared.application.SequenceService;
import com.redsport.backend.shared.domain.model.*;
import com.redsport.backend.stores.application.ports.StoreAccess;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
  private final com.redsport.backend.orders.infrastructure.persistence.jpa.repositories
          .ReceiptRepository
      receipts;
  private final com.redsport.backend.orders.infrastructure.persistence.jpa.repositories
          .CartRepository
      carts;
  private final OrderRepository orders;
  private final Catalog catalog;
  private final CustomerAccounts accounts;
  private final Inventory inventory;
  private final Promotions promos;
  private final Payments payments;
  private final StoreAccess stores;
  private final SequenceService sequence;
  private final Clock clock;

  public OrderService(
      OrderRepository orders,
      Catalog catalog,
      CustomerAccounts accounts,
      Inventory inventory,
      Promotions promos,
      Payments payments,
      StoreAccess stores,
      SequenceService sequence,
      Clock clock,
      com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.ReceiptRepository
          receipts,
      com.redsport.backend.orders.infrastructure.persistence.jpa.repositories.CartRepository
          carts) {
    this.receipts = receipts;
    this.carts = carts;
    this.orders = orders;
    this.catalog = catalog;
    this.accounts = accounts;
    this.inventory = inventory;
    this.promos = promos;
    this.payments = payments;
    this.stores = stores;
    this.sequence = sequence;
    this.clock = clock;
  }

  public record ItemRequest(String sku, int quantity, BigDecimal unitPrice) {}

  public record Checkout(
      String method,
      String fullName,
      String phone,
      String address,
      String district,
      String agency,
      List<ItemRequest> items) {}

  public record PosSale(
      String customerName,
      String customerDoc,
      String paymentMethod,
      BigDecimal cashReceived,
      BigDecimal qrAmount,
      BigDecimal total,
      String discountReason,
      List<ItemRequest> items) {}

  private void key(String key) {
    if (key == null || !key.matches("[a-zA-Z0-9_-]{8,80}"))
      throw new IllegalArgumentException("Idempotency-Key requerido (8 a 80 caracteres)");
  }

  public SalesOrder checkout(String email, String key, Checkout r) {
    key(key);
    var user = accounts.current(email);
    if (!user.role().equals("customer")) throw DomainException.forbidden();
    var existing = orders.findByUserIdAndRequestKey(user.id(), key);
    if (existing.isPresent()) return existing.get();
    if (!Set.of("motorizado", "shalom").contains(r.method()))
      throw new IllegalArgumentException("Método de entrega inválido");
    if (r.fullName() == null || r.fullName().isBlank() || r.phone() == null || r.phone().isBlank())
      throw new IllegalArgumentException("Nombre y teléfono requeridos");
    if (r.method().equals("motorizado") && (blank(r.address()) || blank(r.district())))
      throw new IllegalArgumentException("Dirección y distrito requeridos");
    if (r.method().equals("shalom") && blank(r.agency()))
      throw new IllegalArgumentException("Agencia requerida");
    var lines = quote(r.items(), false, null);
    var order =
        new SalesOrder(
            user.id(),
            key,
            r.method(),
            r.fullName(),
            null,
            r.phone(),
            r.address(),
            r.district(),
            r.agency(),
            lines,
            clock.instant());
    inventory.reserve(order.quantities());
    orders.saveAndFlush(order);
    payments.initialize(order.getId());
    accounts.saveFirstDelivery(user.id(), r.phone(), r.address(), r.district(), r.agency());
    carts.deleteByUserId(user.id());
    return order;
  }

  private boolean blank(String s) {
    return s == null || s.isBlank();
  }

  private List<OrderLine> quote(List<ItemRequest> request, boolean pos, String store) {
    if (request == null || request.isEmpty() || request.size() > 100)
      throw new IllegalArgumentException("El pedido debe tener entre 1 y 100 líneas");
    Map<String, ItemRequest> normalized = new TreeMap<>();
    for (var r : request) {
      var sku = new Sku(r.sku());
      if (r.quantity() <= 0 || r.quantity() > 9999)
        throw new IllegalArgumentException("Cantidad inválida");
      if (pos && !sku.storeCode().equals(store)) throw DomainException.forbidden();
      if (normalized.put(sku.value(), r) != null)
        throw new IllegalArgumentException("SKU repetido");
    }
    var items = catalog.lockItems(normalized.keySet());
    Map<UUID, Promotions.Quote> prices = new HashMap<>();
    items.stream()
        .map(Catalog.Item::productId)
        .distinct()
        .sorted()
        .forEach(
            id -> {
              var group = items.stream().filter(i -> i.productId().equals(id)).toList();
              int n = group.stream().mapToInt(i -> normalized.get(i.sku()).quantity()).sum();
              prices.put(id, promos.reserve(id, group.getFirst().price(), n));
            });
    return items.stream()
        .map(
            i -> {
              var r = normalized.get(i.sku());
              var price = prices.get(i.productId());
              var unit = price.unitPrice();
              if (pos && r.unitPrice() != null) {
                unit = Money.amount(r.unitPrice());
                if (unit.compareTo(price.unitPrice()) > 0)
                  throw new IllegalArgumentException(
                      "El precio negociado no puede superar el precio vigente");
              } else if (!pos && r.unitPrice() != null)
                throw new IllegalArgumentException("El precio lo determina el servidor");
              var line =
                  new OrderLine(
                      i.productId(),
                      i.sku(),
                      i.name(),
                      i.imageUrl(),
                      r.quantity(),
                      unit,
                      i.price(),
                      price.promotionId());
              if (pos && unit.compareTo(price.unitPrice()) < 0) line.markNegotiated();
              return line;
            })
        .toList();
  }

  private SalesOrder locked(UUID id) {
    return orders.lock(id).orElseThrow(() -> DomainException.missing("Pedido no encontrado"));
  }

  private void own(SalesOrder order, String email) {
    if (!order.getUserId().equals(accounts.current(email).id()))
      throw DomainException.missing("Pedido no encontrado");
  }

  private void lockProducts(SalesOrder o) {
    o.getItems().stream()
        .map(OrderLine::getProductId)
        .distinct()
        .sorted()
        .forEach(catalog::lockPrice);
  }

  @Transactional(readOnly = true)
  public List<SalesOrder> mine(String email) {
    return orders.findByUserIdOrderByCreatedAtDesc(accounts.current(email).id());
  }

  @Transactional(readOnly = true)
  public SalesOrder get(String email, UUID id) {
    var o = orders.findById(id).orElseThrow(() -> DomainException.missing("Pedido no encontrado"));
    own(o, email);
    return o;
  }

  @Transactional(readOnly = true)
  public List<SalesOrder> all() {
    return orders.findAll().stream()
        .sorted(Comparator.comparing(SalesOrder::getCreatedAt).reversed())
        .toList();
  }

  public SalesOrder submit(String email, UUID id, String reference) {
    var o = locked(id);
    own(o, email);
    o.requirePayable(clock.instant());
    payments.submit(id, reference);
    return o;
  }

  @Transactional(readOnly = true)
  public Object payment(String email, UUID id) {
    get(email, id);
    return payments.status(id);
  }

  public SalesOrder confirm(String admin, UUID id, String reference, BigDecimal amount) {
    if (!accounts.current(admin).role().equals("admin")) throw DomainException.forbidden();
    var o = locked(id);
    if (o.getPaidAt() != null) {
      payments.confirm(id, reference, o.getTotal(), amount, admin);
      return o;
    }
    o.requirePayable(clock.instant());
    lockProducts(o);
    payments.confirm(id, reference, o.getTotal(), amount, admin);
    promos.commit(o.promotionQuantities());
    inventory.commit(o.quantities());
    recordSales(o);
    accounts.credit(o.getUserId(), o.getTotal());
    o.pay(clock.instant());
    return o;
  }

  private void recordSales(SalesOrder o) {
    Map<UUID, Integer> q = new TreeMap<>();
    o.getItems().forEach(i -> q.merge(i.getProductId(), i.getQuantity(), Integer::sum));
    q.forEach(catalog::recordSale);
  }

  public SalesOrder cancel(String email, UUID id, boolean admin) {
    var o = locked(id);
    if (!admin) {
      own(o, email);
      if (o.getPaidAt() != null)
        throw DomainException.conflict("Contacta con la tienda para cancelar un pedido pagado");
    } else if (!accounts.current(email).role().equals("admin")) throw DomainException.forbidden();
    if (o.getStatus().equals("CANCELLED")) return o;
    if (o.getStatus().equals("PENDING")) {
      lockProducts(o);
      promos.release(o.promotionQuantities());
      inventory.release(o.quantities());
    }
    // Paid cancellations never refund points or automatically restock dispatched merchandise.
    o.cancel();
    return o;
  }

  public boolean expire(UUID id) {
    var o = locked(id);
    if (!o.getStatus().equals("PENDING") || clock.instant().isBefore(o.getQrExpiresAt()))
      return false;
    lockProducts(o);
    promos.release(o.promotionQuantities());
    inventory.release(o.quantities());
    o.expire(clock.instant());
    return true;
  }

  public SalesOrder renew(String email, UUID id) {
    var o = locked(id);
    own(o, email);
    if (o.getStatus().equals("PENDING") && !clock.instant().isBefore(o.getQrExpiresAt())) {
      lockProducts(o);
      promos.release(o.promotionQuantities());
      inventory.release(o.quantities());
      o.expire(clock.instant());
    }
    if (!o.getStatus().equals("EXPIRED"))
      throw DomainException.conflict("El pedido no está vencido");
    var request =
        o.getItems().stream().map(i -> new ItemRequest(i.getSku(), i.getQuantity(), null)).toList();
    var lines = quote(request, false, null);
    o.renew(lines, clock.instant());
    inventory.reserve(o.quantities());
    payments.reset(id);
    return o;
  }

  @Transactional(readOnly = true)
  public Object reviewPayment(String email, UUID id) {
    if (!accounts.current(email).role().equals("admin")) throw DomainException.forbidden();
    return payments.status(id);
  }

  public void rejectPayment(String email, UUID id, String note) {
    if (!accounts.current(email).role().equals("admin")) throw DomainException.forbidden();
    var order = locked(id);
    if (order.getPaidAt() != null) throw DomainException.conflict("Pago confirmado");
    payments.reject(id, email, note);
  }

  public SalesOrder advance(UUID id) {
    var o = locked(id);
    o.advance();
    return o;
  }

  public SalesOrder pos(String email, String key, PosSale r) {
    key(key);
    var store = stores.requireOperator(email);
    var user = accounts.current(email);
    var existing = orders.findByUserIdAndRequestKey(user.id(), key);
    if (existing.isPresent()) return existing.get();
    var lines = quote(r.items(), true, store.code());
    var o =
        new SalesOrder(
            user.id(),
            key,
            "tienda",
            blank(r.customerName()) ? "Clientes Varios" : r.customerName(),
            blank(r.customerDoc()) ? "00000001" : r.customerDoc(),
            null,
            null,
            null,
            null,
            lines,
            clock.instant());
    if (!o.getCustomerDoc().matches("[0-9]{8}")) throw new IllegalArgumentException("DNI inválido");
    var number =
        store.boletaSeries()
            + "-"
            + String.format("%08d", sequence.next("receipt:" + store.code(), 0));
    o.pos(
        store.code(),
        store.managerName(),
        number,
        r.paymentMethod(),
        r.cashReceived(),
        r.qrAmount(),
        r.total(),
        r.discountReason(),
        clock.instant());
    promos.commit(o.promotionQuantities());
    inventory.reserve(o.quantities());
    inventory.commit(o.quantities());
    recordSales(o);
    orders.saveAndFlush(o);
    receipts.save(
        new com.redsport.backend.orders.domain.model.aggregates.Receipt(
            o.getId(),
            number,
            o.getCustomerName(),
            o.getCustomerDoc(),
            com.redsport.backend.orders.domain.services.AmountInWords.soles(o.getTotal()),
            store.address()));
    return o;
  }

  @Transactional(readOnly = true)
  public Object receipt(String email, UUID id) {
    var store = stores.requireOperator(email);
    var o = orders.findById(id).orElseThrow(() -> DomainException.missing("Venta no encontrada"));
    if (!store.code().equals(o.getStoreCode()))
      throw DomainException.missing("Venta no encontrada");
    return new ReceiptResource(
        receipts.findById(id).orElseThrow(() -> DomainException.missing("Boleta no encontrada")),
        com.redsport.backend.orders.application.queries.OrderView.from(o));
  }

  public record ReceiptResource(
      com.redsport.backend.orders.domain.model.aggregates.Receipt receipt,
      com.redsport.backend.orders.application.queries.OrderView order) {}

  @Transactional(readOnly = true)
  public List<SalesOrder> sales(String email, LocalDate from, LocalDate to, String query) {
    var store = stores.requireOperator(email);
    if (from != null && to != null && from.isAfter(to))
      throw new IllegalArgumentException("Intervalo de fechas inválido");
    String term = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    return orders.findByStoreCodeOrderByCreatedAtDesc(store.code()).stream()
        .filter(
            o -> {
              var date = o.getPaidAt().atZone(ZoneId.of("America/Lima")).toLocalDate();
              return (from == null || !date.isBefore(from))
                  && (to == null || !date.isAfter(to))
                  && (term.isBlank()
                      || (o.getCustomerName() + " " + o.getCode() + " " + o.getBoletaNumber())
                          .toLowerCase(Locale.ROOT)
                          .contains(term)
                      || o.getItems().stream()
                          .anyMatch(i -> i.getSku().toLowerCase(Locale.ROOT).contains(term)));
            })
        .toList();
  }
}
