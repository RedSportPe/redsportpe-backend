package com.redsport.backend.stores.interfaces.rest.transform;

import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.interfaces.rest.resources.StoreResource;

public class StoreResourceFromEntityAssembler {
    public static StoreResource toResourceFromEntity(Store store) {
        return new StoreResource(
                store.getId().toString(),
                store.getName(),
                store.getCode(),
                store.getBoletaSeries(),
                store.getAddress(),
                store.getManagerName(),
                store.getDistrict(),
                store.getProvince(),
                store.getDepartment(),
                store.getPhone(),
                store.isActive()
        );
    }
}