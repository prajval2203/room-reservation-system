package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
