package com.redsport.backend.stores.domain.services;

import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.queries.GetAllActiveStoresQuery;
import java.util.List;

public interface StoreQueryService {
    List<Store> handle(GetAllActiveStoresQuery query);
}