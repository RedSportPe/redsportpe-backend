package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.domain.model.queries.GetAllPublishedProductsQuery;
import com.redsport.backend.catalog.domain.services.ProductQueryService;
import com.redsport.backend.catalog.interfaces.rest.resources.ProductResource;
import com.redsport.backend.catalog.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json")
public class ProductsController {
    private final ProductQueryService productQueryService;

    public ProductsController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResource>> getAllProducts() {
        var products = productQueryService.handle(new GetAllPublishedProductsQuery());
        var resources = products.stream()
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}