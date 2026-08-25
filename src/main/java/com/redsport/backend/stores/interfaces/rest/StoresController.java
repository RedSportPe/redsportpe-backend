package com.redsport.backend.stores.interfaces.rest;

import com.redsport.backend.stores.domain.model.commands.CreateStoreWithOperatorCommand;
import com.redsport.backend.stores.domain.model.commands.DeleteStoreCommand;
import com.redsport.backend.stores.domain.model.commands.UpdateOperatorCommand;
import com.redsport.backend.stores.domain.model.queries.GetAllActiveStoresQuery;
import com.redsport.backend.stores.domain.services.StoreCommandService;
import com.redsport.backend.stores.domain.services.StoreQueryService;
import com.redsport.backend.stores.interfaces.rest.resources.CreateStoreResource;
import com.redsport.backend.stores.interfaces.rest.resources.StoreResource;
import com.redsport.backend.stores.interfaces.rest.resources.UpdateOperatorResource;
import com.redsport.backend.stores.interfaces.rest.transform.StoreResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/stores", produces = "application/json")
public class StoresController {

    private final StoreQueryService storeQueryService;
    private final StoreCommandService storeCommandService;

    public StoresController(StoreQueryService storeQueryService,
                            StoreCommandService storeCommandService) {
        this.storeQueryService = storeQueryService;
        this.storeCommandService = storeCommandService;
    }

    @GetMapping
    public ResponseEntity<List<StoreResource>> getAllStores() {
        var stores = storeQueryService.handle(new GetAllActiveStoresQuery());
        var resources = stores.stream()
                .map(StoreResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<StoreResource> createStore(@RequestBody CreateStoreResource resource) {
        var command = new CreateStoreWithOperatorCommand(
                resource.name(), resource.address(), resource.managerName(),
                resource.district(), resource.province(), resource.department(),
                resource.phone(), resource.operatorEmail(), resource.operatorPassword()
        );
        var store = storeCommandService.handle(command);
        var storeResource = StoreResourceFromEntityAssembler.toResourceFromEntity(store);
        return ResponseEntity.status(201).body(storeResource);
    }

    /** Change the cashier credential of a store */
    @PatchMapping("/{storeId}/operator")
    public ResponseEntity<StoreResource> updateOperator(
            @PathVariable String storeId,
            @RequestBody UpdateOperatorResource resource) {
        var command = new UpdateOperatorCommand(
                UUID.fromString(storeId),
                resource.name(), resource.email(), resource.password()
        );
        var store = storeCommandService.handle(command);
        var storeResource = StoreResourceFromEntityAssembler.toResourceFromEntity(store);
        return ResponseEntity.ok(storeResource);
    }

    /** Soft delete a store (deactivate, preserve fiscal trail) */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeId) {
        storeCommandService.handle(new DeleteStoreCommand(UUID.fromString(storeId)));
        return ResponseEntity.noContent().build();
    }
}