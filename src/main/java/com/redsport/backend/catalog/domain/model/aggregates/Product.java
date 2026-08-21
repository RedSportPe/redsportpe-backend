package com.redsport.backend.catalog.domain.model.aggregates;

import com.redsport.backend.catalog.domain.model.entities.Variant;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Product is the aggregate root of the Catalog context. It owns its variants and
 * guards its own invariants — behaviour lives here, not in services or controllers.
 */
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

    @Column(name = "category", length = 60, nullable = false)
    private String category;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private List<Variant> variants;

    protected Product() { } // required by JPA

    // ===== Behaviour: the aggregate protects its own rules =====

    /** A product can only be featured (trends carousel) if it's published */
    public boolean isVisibleInTrends() {
        return published && featured;
    }

    /** Total available stock across all its variants */
    public int totalAvailableStock() {
        if (variants == null) return 0;
        return variants.stream()
                .filter(v -> v.getTotalStock() != null)
                .mapToInt(Variant::getTotalStock)
                .sum();
    }

    public boolean isSoldOut() {
        return totalAvailableStock() == 0;
    }

    /** Never expose the mutable internal list */
    public List<Variant> getVariants() {
        return variants == null ? List.of() : Collections.unmodifiableList(variants);
    }
}