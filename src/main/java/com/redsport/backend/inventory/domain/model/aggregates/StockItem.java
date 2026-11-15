package com.redsport.backend.inventory.domain.model.aggregates;

import com.redsport.backend.shared.domain.model.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "inventory_stock")
@Getter
public class StockItem {
  @Id
  @Column(length = 60)
  private String sku;

  @Column(nullable = false)
  private int onHand;

  @Column(nullable = false)
  private int reserved;

  @Version private long version;

  protected StockItem() {}

  public StockItem(String sku, int quantity) {
    this.sku = sku;
    adjust(quantity);
  }

  public int available() {
    return onHand - reserved;
  }

  public void adjust(int quantity) {
    if (quantity < reserved || quantity < 0)
      throw DomainException.conflict("El stock no puede ser menor a las unidades reservadas");
    onHand = quantity;
  }

  public void reserve(int quantity) {
    positive(quantity);
    if (quantity > available()) throw DomainException.conflict("Stock insuficiente: " + sku);
    reserved = Math.addExact(reserved, quantity);
  }

  public void release(int quantity) {
    positive(quantity);
    if (quantity > reserved) throw DomainException.conflict("Reserva inválida");
    reserved -= quantity;
  }

  public void commit(int quantity) {
    release(quantity);
    onHand -= quantity;
  }

  private void positive(int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("Cantidad inválida");
  }
}
