package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
