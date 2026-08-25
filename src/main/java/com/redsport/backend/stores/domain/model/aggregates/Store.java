package com.redsport.backend.stores.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import java.util.UUID;

/**
 * Store is the permanent, central entity. Its operator (the access credential)
 * is linked via store_operators. Deleting a store is a soft delete (active=false)
 * to preserve the fiscal trail (boleta series must never be reused).
 */
@Entity
@Table(name = "stores")
@Getter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "boleta_series", length = 10)
    private String boletaSeries;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "manager_name", length = 150)
    private String managerName;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Store() { }

    /** Factory: create a new active store with generated code + boleta series */
    public Store(String name, String code, String boletaSeries, String address,
                 String managerName, String district, String province,
                 String department, String phone) {
        this.name = name;
        this.code = code;
        this.boletaSeries = boletaSeries;
        this.address = address;
        this.managerName = managerName;
        this.district = district;
        this.province = province;
        this.department = department;
        this.phone = phone;
        this.active = true;
    }

    /** Soft delete: preserves the row (and its fiscal trail) */
    public void deactivate() {
        this.active = false;
    }

    public void updateDetails(String name, String address, String managerName,
                              String district, String province, String department, String phone) {
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.district = district;
        this.province = province;
        this.department = department;
        this.phone = phone;
    }
}