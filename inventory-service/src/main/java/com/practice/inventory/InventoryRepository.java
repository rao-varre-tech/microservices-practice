package com.practice.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    // Atomic: only decrements when enough stock exists. Returns rows updated (0 = not enough stock).
    @Modifying(clearAutomatically = true)
    @Query("update Inventory i set i.quantity = i.quantity - :qty where i.productId = :productId and i.quantity >= :qty")
    int reserve(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("update Inventory i set i.quantity = i.quantity + :qty where i.productId = :productId")
    int release(@Param("productId") Long productId, @Param("qty") int qty);
}
