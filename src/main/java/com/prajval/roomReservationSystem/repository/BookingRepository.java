package com.prajval.roomReservationSystem.repository;

import com.prajval.roomReservationSystem.entity.Booking;
import com.prajval.roomReservationSystem.entity.Hotel;
import com.prajval.roomReservationSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> findByUser(User user);
}
