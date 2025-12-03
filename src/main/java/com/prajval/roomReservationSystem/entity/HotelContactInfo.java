package com.prajval.roomReservationSystem.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class HotelContactInfo {

    private String address;

    private String email;

    private Long phoneNumber;

    private String location;
}
