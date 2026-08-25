package com.redsport.backend.stores.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import java.io.Serializable;
import java.util.UUID;

/**
 * Links a store with its operator (a user). Composite PK (store_id, user_id).
 * One store ↔ one operator in this business model.
 */
@Entity
@Table(name = "store_operators")
@Getter
@IdClass(StoreOperator.StoreOperatorId.class)
public class StoreOperator {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    protected StoreOperator() { }

    public StoreOperator(UUID storeId, UUID userId) {
        this.storeId = storeId;
        this.userId = userId;
    }

    public static class StoreOperatorId implements Serializable {
        private UUID storeId;
        private UUID userId;
        public StoreOperatorId() { }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StoreOperatorId that)) return false;
            return java.util.Objects.equals(storeId, that.storeId)
                    && java.util.Objects.equals(userId, that.userId);
        }
        @Override public int hashCode() { return java.util.Objects.hash(storeId, userId); }
    }
}