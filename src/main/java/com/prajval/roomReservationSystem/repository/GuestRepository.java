package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}