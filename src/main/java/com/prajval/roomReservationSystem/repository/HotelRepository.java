package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Hotel;
import com.prajval.roomReservationSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}
