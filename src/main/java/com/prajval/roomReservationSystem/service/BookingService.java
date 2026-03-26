package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.dto.BookingDto;
import com.prajval.roomReservationSystem.dto.BookingRequestDto;
import com.prajval.roomReservationSystem.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestIdList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);
}
