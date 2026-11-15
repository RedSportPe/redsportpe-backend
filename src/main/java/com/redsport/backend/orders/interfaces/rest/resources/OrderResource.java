package com.redsport.backend.orders.interfaces.rest.resources;

import com.redsport.backend.orders.domain.model.aggregates.SalesOrder;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record OrderResource(
    UUID id,
    String code,
    UUID userId,
    String status,
    String method,
    BigDecimal total,
    BigDecimal subtotal,
    String discountReason,
    Shipping shipping,
    Instant createdAt,
    Instant qrExpiresAt,
    Instant paidAt,
    LocalDate deliveryDate,
    int trackingStep,
    String paymentMethod,
    BigDecimal cashReceived,
    BigDecimal qrAmount,
    String sellerName,
    String storeCode,
    String boletaNumber,
    String customerDoc,
    List<Line> items,
    long version) {
  public record Shipping(
      String fullName, String phone, String address, String district, String agency) {}

  public record Line(
      UUID productId,
      String sku,
      String productName,
      String imageUrl,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal catalogPrice) {}

  public static OrderResource from(SalesOrder o) {
    return new OrderResource(
        o.getId(),
        o.getCode(),
        o.getUserId(),
        o.getStatus(),
        o.getMethod(),
        o.getTotal(),
        o.getSubtotal(),
        o.getDiscountReason(),
        new Shipping(
            o.getCustomerName(), o.getPhone(), o.getAddress(), o.getDistrict(), o.getAgency()),
        o.getCreatedAt(),
        o.getQrExpiresAt(),
        o.getPaidAt(),
        o.getDeliveryDate(),
        o.getTrackingStep(),
        o.getPaymentMethod(),
        o.getCashReceived(),
        o.getQrAmount(),
        o.getSellerName(),
        o.getStoreCode(),
        o.getBoletaNumber(),
        o.getCustomerDoc(),
        o.getItems().stream()
            .map(
                i ->
                    new Line(
                        i.getProductId(),
                        i.getSku(),
                        i.getProductName(),
                        i.getImageUrl(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getCatalogPrice()))
            .toList(),
        o.getVersion());
  }
}
