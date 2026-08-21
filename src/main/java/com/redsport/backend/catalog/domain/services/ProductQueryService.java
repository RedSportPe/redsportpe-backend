package com.redsport.backend.catalog.domain.services;

import com.redsport.backend.catalog.domain.model.aggregates.Product;
import com.redsport.backend.catalog.domain.model.queries.GetAllPublishedProductsQuery;
import java.util.List;

public interface ProductQueryService {
    List<Product> handle(GetAllPublishedProductsQuery query);
}