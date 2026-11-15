package com.redsport.backend.identity.interfaces.rest;

import com.redsport.backend.identity.application.internal.ProfileService;
import com.redsport.backend.identity.application.ports.CustomerAccounts;
import com.redsport.backend.identity.domain.model.aggregates.DeliveryProfile;
import com.redsport.backend.stores.application.ports.StoreAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {
  private final CustomerAccounts accounts;
  private final ProfileService profiles;
  private final StoreAccess stores;

  public ProfileController(CustomerAccounts accounts, ProfileService profiles, StoreAccess stores) {
    this.accounts = accounts;
    this.profiles = profiles;
    this.stores = stores;
  }

  @GetMapping
  public Object me(Principal p) {
    var u = accounts.current(p.getName());
    var store = stores.assignedStore(p.getName()).orElse(null);
    return new ProfileResource(
        u.id(),
        u.name(),
        u.email(),
        u.role(),
        u.points(),
        store == null ? null : store.code(),
        store);
  }

  @GetMapping("/delivery-info")
  public DeliveryProfile delivery(Principal p) {
    return profiles.get(p.getName());
  }

  @PutMapping("/delivery-info")
  public DeliveryProfile save(Principal p, @Valid @RequestBody DeliveryResource r) {
    return profiles.save(p.getName(), r.phone(), r.address(), r.district(), r.agency());
  }

  public record ProfileResource(
      java.util.UUID id,
      String name,
      String email,
      String role,
      java.math.BigDecimal points,
      String storeCode,
      StoreAccess.StoreInfo store) {}

  public record DeliveryResource(
      @NotBlank @Size(max = 20) String phone,
      @Size(max = 200) String address,
      @Size(max = 100) String district,
      @Size(max = 200) String agency) {}
}
