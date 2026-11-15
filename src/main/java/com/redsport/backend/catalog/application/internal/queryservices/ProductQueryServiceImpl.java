package com.redsport.backend.catalog.application.internal.queryservices;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import com.redsport.backend.catalog.domain.model.queries.GetAllPublishedProductsQuery;
import com.redsport.backend.catalog.domain.services.ProductQueryService;
import com.redsport.backend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {
  private final ProductRepository productRepository;

  public ProductQueryServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public List<Product> handle(GetAllPublishedProductsQuery query) {
    return productRepository.findByPublishedTrueAndDeletedFalse();
  }
}
