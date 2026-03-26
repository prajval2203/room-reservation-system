package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.entity.Booking;

public interface CheckOutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);


}
