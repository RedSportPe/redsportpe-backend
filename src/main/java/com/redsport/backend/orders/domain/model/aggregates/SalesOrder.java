package com.redsport.backend.orders.domain.model.aggregates;

import com.redsport.backend.orders.domain.model.entities.OrderLine;
import com.redsport.backend.shared.domain.model.*;
import com.redsport.backend.shipping.domain.services.DeliveryRules;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;

@Entity
@Table(
    name = "orders",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "request_key"})})
@Getter
public class SalesOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 80)
  private String requestKey;

  @Column(nullable = false, unique = true, length = 40)
  private String code;

  @Column(nullable = false, length = 20)
  private String status;

  @org.hibernate.annotations.ColumnTransformer(write = "?::order_method")
  @Column(nullable = false, length = 20)
  private String method;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(length = 300)
  private String discountReason;

  @Column(nullable = false, length = 150)
  private String customerName;

  @Column(length = 20)
  private String customerDoc;

  @Column(length = 20)
  private String phone;

  @Column(length = 200)
  private String address;

  @Column(length = 100)
  private String district;

  @Column(length = 200)
  private String agency;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant qrExpiresAt;

  private Instant paidAt;
  private LocalDate deliveryDate;

  @Column(nullable = false)
  private short trackingStep;

  @org.hibernate.annotations.ColumnTransformer(write = "?::order_payment_method")
  @Column(length = 20)
  private String paymentMethod;

  @Column(precision = 12, scale = 2)
  private BigDecimal cashReceived;

  @Column(precision = 12, scale = 2)
  private BigDecimal qrAmount;

  @Column(length = 150)
  private String sellerName;

  @Column(length = 10)
  private String storeCode;

  @Column(length = 30, unique = true)
  private String boletaNumber;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "order_id", nullable = false)
  @OrderColumn(name = "line_position")
  private List<OrderLine> items = new ArrayList<>();

  @Version private long version;

  protected SalesOrder() {}

  public SalesOrder(
      UUID userId,
      String key,
      String method,
      String name,
      String doc,
      String phone,
      String address,
      String district,
      String agency,
      List<OrderLine> lines,
      Instant now) {
    if (!Set.of("motorizado", "shalom", "tienda").contains(method))
      throw new IllegalArgumentException("Método inválido");
    this.userId = userId;
    requestKey = key;
    code = "RS-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    status = "PENDING";
    this.method = method;
    customerName = name;
    customerDoc = doc;
    this.phone = phone;
    this.address = address;
    this.district = district;
    this.agency = agency;
    createdAt = now;
    qrExpiresAt = now.plus(Duration.ofHours(8));
    replaceLines(lines);
  }

  public void replaceLines(List<OrderLine> lines) {
    if (lines.isEmpty()) throw new IllegalArgumentException("Pedido vacío");
    items.clear();
    items.addAll(lines);
    total = items.stream().map(OrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    subtotal =
        items.stream()
            .map(i -> i.getCatalogPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    Money.positive(total);
  }

  public void renew(List<OrderLine> lines, Instant now) {
    if (!status.equals("EXPIRED")) throw DomainException.conflict("El pedido no está vencido");
    replaceLines(lines);
    status = "PENDING";
    qrExpiresAt = now.plus(Duration.ofHours(8));
  }

  public void requirePayable(Instant now) {
    if (!status.equals("PENDING") || !now.isBefore(qrExpiresAt))
      throw DomainException.conflict("El pedido no admite pagos; renueva el QR si venció");
  }

  public void pay(Instant now) {
    requirePayable(now);
    status = "PAID";
    paidAt = now;
    paymentMethod = "qr";
    deliveryDate = DeliveryRules.deliveryDate(method, now);
  }

  public void expire(Instant now) {
    if (!status.equals("PENDING") || now.isBefore(qrExpiresAt))
      throw DomainException.conflict("El pedido no ha vencido");
    status = "EXPIRED";
  }

  public void cancel() {
    if (Set.of("DELIVERED", "CANCELLED").contains(status))
      throw DomainException.conflict("No se puede cancelar este pedido");
    status = "CANCELLED";
  }

  public void advance() {
    if (!status.equals("PAID"))
      throw DomainException.conflict("Solo se pueden despachar pedidos pagados");
    trackingStep++;
    if (trackingStep == 3) status = "DELIVERED";
  }

  public void pos(
      String storeCode,
      String seller,
      String receipt,
      String payment,
      BigDecimal cash,
      BigDecimal qr,
      BigDecimal override,
      String reason,
      Instant now) {
    BigDecimal before = total;
    if (override != null) {
      total = Money.positive(override);
      if (total.compareTo(before) > 0)
        throw new IllegalArgumentException(
            "El total negociado no puede superar el precio del ticket");
    }
    boolean negotiated =
        items.stream().anyMatch(OrderLine::isNegotiated) || total.compareTo(before) < 0;
    if (negotiated && (reason == null || reason.isBlank()))
      throw new IllegalArgumentException("Indica el motivo del descuento");
    this.discountReason = reason;
    this.storeCode = storeCode;
    sellerName = seller;
    boletaNumber = receipt;
    paymentMethod = payment;
    cashReceived = cash == null ? BigDecimal.ZERO : Money.amount(cash);
    qrAmount = qr == null ? BigDecimal.ZERO : Money.amount(qr);
    switch (payment) {
      case "efectivo" -> {
        if (cashReceived.compareTo(total) < 0 || qrAmount.signum() != 0)
          throw new IllegalArgumentException("Efectivo insuficiente o QR inesperado");
      }
      case "qr" -> {
        if (cashReceived.signum() != 0)
          throw new IllegalArgumentException("Pago QR no admite efectivo");
        qrAmount = total;
      }
      case "mixto" -> {
        if (cashReceived.signum() <= 0
            || qrAmount.signum() <= 0
            || cashReceived.add(qrAmount).compareTo(total) != 0)
          throw new IllegalArgumentException("El pago mixto debe sumar el total");
      }
      default -> throw new IllegalArgumentException("Método de pago inválido");
    }
    paidAt = now;
    status = "DELIVERED";
    trackingStep = 3;
    deliveryDate = DeliveryRules.deliveryDate("tienda", now);
  }

  public Map<String, Integer> quantities() {
    Map<String, Integer> q = new TreeMap<>();
    items.forEach(i -> q.merge(i.getSku(), i.getQuantity(), Integer::sum));
    return q;
  }

  public Map<UUID, Integer> promotionQuantities() {
    Map<UUID, Integer> q = new TreeMap<>();
    items.stream()
        .filter(i -> i.getPromotionId() != null)
        .forEach(i -> q.merge(i.getPromotionId(), i.getQuantity(), Integer::sum));
    return q;
  }
}
