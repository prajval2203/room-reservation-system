package com.prajval.roomReservationSystem.dto;

import com.prajval.roomReservationSystem.entity.Hotel;
import com.prajval.roomReservationSystem.entity.Room;
import com.prajval.roomReservationSystem.entity.User;
import com.prajval.roomReservationSystem.entity.enums.BookingStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


@Data
public class BookingDto {

    private Long id;
//    private Hotel hotel;
//    private Room room;
//    private User user;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
}
