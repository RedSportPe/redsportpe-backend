package com.redsport.backend.orders.domain.model.aggregates;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "receipts")
@Getter
public class Receipt {
  @Id private UUID orderId;

  @Column(name = "boleta_code", nullable = false, length = 30)
  private String boletaNumber;

  @Column(length = 150)
  private String customerName;

  @Column(name = "customer_dni", length = 15)
  private String customerDoc;

  @Column(nullable = false, length = 200)
  private String amountInWords;

  @Column(length = 200)
  private String storeAddress;

  private Instant printedAt;
  private int reprintCount;

  protected Receipt() {}

  public Receipt(
      UUID orderId, String number, String name, String doc, String words, String address) {
    this.orderId = orderId;
    boletaNumber = number;
    customerName = name;
    customerDoc = doc;
    amountInWords = words;
    storeAddress = address;
  }
}
