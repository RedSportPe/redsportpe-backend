package com.redsport.backend.catalog.application.internal.queryservices;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import com.redsport.backend.catalog.domain.model.queries.GetAllPublishedProductsQuery;
import com.redsport.backend.catalog.domain.services.ProductQueryService;
import com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> handle(GetAllPublishedProductsQuery query) {
        return productRepository.findByPublishedTrue();
    }
}