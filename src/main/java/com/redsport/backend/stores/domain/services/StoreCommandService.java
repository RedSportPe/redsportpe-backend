package com.redsport.backend.stores.domain.services;

import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.commands.CreateStoreWithOperatorCommand;
import com.redsport.backend.stores.domain.model.commands.DeleteStoreCommand;
import com.redsport.backend.stores.domain.model.commands.UpdateOperatorCommand;

public interface StoreCommandService {
  Store handle(CreateStoreWithOperatorCommand command);

  Store handle(UpdateOperatorCommand command);

  Store handle(com.redsport.backend.stores.domain.model.commands.UpdateStoreCommand command);

  void handle(DeleteStoreCommand command);
}
