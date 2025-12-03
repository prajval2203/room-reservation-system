package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
