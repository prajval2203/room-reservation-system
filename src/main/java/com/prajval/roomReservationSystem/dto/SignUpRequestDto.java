package com.prajval.roomReservationSystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SignUpRequestDto {

    private String email;
    private String name;
    private String password;
    private String gender;
    private LocalDate dateOfBirth;
}
