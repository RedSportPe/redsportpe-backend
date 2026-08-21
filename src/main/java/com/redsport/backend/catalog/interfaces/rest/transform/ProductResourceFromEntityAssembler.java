package com.redsport.backend.catalog.interfaces.rest.transform;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import com.redsport.backend.catalog.interfaces.rest.resources.ProductResource;
import com.redsport.backend.catalog.interfaces.rest.resources.VariantResource;
import java.util.List;

/** Translates the domain aggregate into an API resource. The entity never leaves the domain. */
public class ProductResourceFromEntityAssembler {

    public static ProductResource toResourceFromEntity(Product product) {
        List<VariantResource> variants = product.getVariants().stream()
                .map(v -> new VariantResource(
                        v.getSku().value(),
                        v.getGender(),
                        v.getSize(),
                        v.getColor(),
                        v.getTotalStock()
                ))
                .toList();

        return new ProductResource(
                product.getId().toString(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                product.isPublished(),
                product.isFeatured(),
                product.getSalesCount(),
                product.getCreatedAt() != null ? product.getCreatedAt().toString() : null,
                variants
        );
    }
}