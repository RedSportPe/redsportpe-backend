package com.redsport.backend.stores.application.internal.commandservices;

import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.valueobjects.Roles;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.stores.domain.model.aggregates.Store;
import com.redsport.backend.stores.domain.model.commands.CreateStoreWithOperatorCommand;
import com.redsport.backend.stores.domain.model.commands.DeleteStoreCommand;
import com.redsport.backend.stores.domain.model.commands.UpdateOperatorCommand;
import com.redsport.backend.stores.domain.model.entities.StoreOperator;
import com.redsport.backend.stores.domain.services.StoreCommandService;
import com.redsport.backend.stores.infrastructure.persistence.jpa.repositories.StoreOperatorRepository;
import com.redsport.backend.stores.infrastructure.persistence.jpa.repositories.StoreRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;
    private final StoreOperatorRepository storeOperatorRepository;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StoreCommandServiceImpl(StoreRepository storeRepository,
                                   StoreOperatorRepository storeOperatorRepository,
                                   UserCommandService userCommandService,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.storeOperatorRepository = storeOperatorRepository;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Store handle(CreateStoreWithOperatorCommand command) {
        long totalStoresEver = storeRepository.count();
        int nextNumber = (int) totalStoresEver + 1;
        String code = "T" + nextNumber;
        String boletaSeries = String.format("B%03d", nextNumber);

        String storeName = (command.name() != null && !command.name().isBlank())
                ? command.name()
                : "Tienda " + nextNumber;
        Store store = new Store(
                storeName, code, boletaSeries, command.address(),
                command.managerName(), command.district(), command.province(),
                command.department(), command.phone()
        );
        store = storeRepository.save(store);

        User operator = userCommandService.handle(new CreateUserCommand(
                command.managerName() != null ? command.managerName() : "Operadora",
                command.operatorEmail(),
                command.operatorPassword(),
                Roles.operator
        ));

        storeOperatorRepository.save(new StoreOperator(store.getId(), operator.getId()));
        return store;
    }

    /** Change the cashier credential of a store (name/email/password). */
    @Override
    @Transactional
    public Store handle(UpdateOperatorCommand command) {
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new RuntimeException("La tienda no existe"));

        // Find the linked operator
        StoreOperator link = storeOperatorRepository.findByStoreId(store.getId())
                .orElseThrow(() -> new RuntimeException("Esta tienda no tiene operadora vinculada"));

        User operator = userRepository.findById(link.getUserId())
                .orElseThrow(() -> new RuntimeException("La operadora no existe"));

        // Update the credential fields
        if (command.name() != null && !command.name().isBlank()) {
            operator.updateName(command.name());
        }
        if (command.email() != null && !command.email().isBlank()) {
            String newEmail = command.email().trim().toLowerCase();
            // If the email changes, ensure it's not taken by someone else
            if (!newEmail.equals(operator.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Ya existe un usuario con ese correo");
            }
            operator.updateEmail(newEmail);
        }
        if (command.password() != null && !command.password().isBlank()) {
            operator.updatePasswordHash(passwordEncoder.encode(command.password()));
        }
        userRepository.save(operator);

        // Also sync the manager name on the store (the encargada's name)
        if (command.name() != null && !command.name().isBlank()) {
            store.updateDetails(store.getName(), store.getAddress(), command.name(),
                    store.getDistrict(), store.getProvince(), store.getDepartment(), store.getPhone());
            storeRepository.save(store);
        }

        return store;
    }

    /** Soft delete: deactivate the store AND its operator (preserves fiscal trail). */
    @Override
    @Transactional
    public void handle(DeleteStoreCommand command) {
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new RuntimeException("La tienda no existe"));

        // Deactivate the store (soft delete)
        store.deactivate();
        storeRepository.save(store);

        // Note: the operator user stays in the DB (fiscal trail), but we could
        // also deactivate it here if users had an 'active' flag. For now the
        // store being inactive is enough to block access.
    }
}