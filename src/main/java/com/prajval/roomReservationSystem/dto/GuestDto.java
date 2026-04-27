package com.prajval.roomReservationSystem.dto;

import com.prajval.roomReservationSystem.entity.User;
import com.prajval.roomReservationSystem.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
    //changed
    private UserDto user;
    private String name;
    private Gender gender;
    private Integer age;
}
