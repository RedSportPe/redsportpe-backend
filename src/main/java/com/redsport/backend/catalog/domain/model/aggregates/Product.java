package com.redsport.backend.catalog.domain.model.aggregates;

import com.redsport.backend.catalog.domain.model.entities.ProductImage;
import com.redsport.backend.catalog.domain.model.entities.Variant;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", length = 10, nullable = false)
    private String productCode;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 60)
    private String category;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "published")
    private boolean published;

    @Column(name = "featured")
    private boolean featured;

    @Column(name = "sales_count")
    private Integer salesCount;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private List<Variant> variants;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    @OrderBy("position ASC")
    private List<ProductImage> images;

    protected Product() { }

    public boolean isVisibleInTrends() {
        return published && featured;
    }

    public String coverImageUrl() {
        if (images == null || images.isEmpty()) return null;
        return images.get(0).getUrl();
    }

    public List<String> imageUrls() {
        if (images == null) return List.of();
        return images.stream().map(ProductImage::getUrl).toList();
    }

    public List<Variant> getVariants() {
        return variants == null ? List.of() : Collections.unmodifiableList(variants);
    }
}