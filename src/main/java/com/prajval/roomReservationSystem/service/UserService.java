package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.dto.ProfileUpdateRequestDto;
import com.prajval.roomReservationSystem.dto.UserDto;
import com.prajval.roomReservationSystem.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
