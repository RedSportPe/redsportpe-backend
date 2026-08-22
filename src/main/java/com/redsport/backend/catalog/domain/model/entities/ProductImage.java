package com.redsport.backend.catalog.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import java.util.UUID;

@Entity
@Table(name = "product_images")
@Getter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "position")
    private Short position;

    protected ProductImage() { }
}