package com.redsport.backend.identity.domain.model.aggregates;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "delivery_info")
@Getter
public class DeliveryProfile {
  @Id private UUID userId;

  @Column(length = 20, nullable = false)
  private String phone;

  @Column(length = 200)
  private String address;

  @Column(length = 100)
  private String district;

  @Column(length = 200)
  private String agency;

  protected DeliveryProfile() {}

  public DeliveryProfile(
      UUID userId, String phone, String address, String district, String agency) {
    this.userId = userId;
    this.phone = phone;
    this.address = address;
    this.district = district;
    this.agency = agency;
  }
}
