package com.redsport.backend.stores.application.internal.queryservices;

import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.queries.GetAllActiveStoresQuery;
import com.redsport.backend.stores.domain.services.StoreQueryService;
import com.redsport.backend.stores.infrastructure.persistence.jpa.repositories.StoreRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StoreQueryServiceImpl implements StoreQueryService {
    private final StoreRepository storeRepository;

    public StoreQueryServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public List<Store> handle(GetAllActiveStoresQuery query) {
        return storeRepository.findByActiveTrue();
    }
}