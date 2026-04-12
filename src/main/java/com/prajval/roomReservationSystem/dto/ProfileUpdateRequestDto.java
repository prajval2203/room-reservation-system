package com.prajval.roomReservationSystem.dto;

import com.prajval.roomReservationSystem.entity.enums.Gender;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {

    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
