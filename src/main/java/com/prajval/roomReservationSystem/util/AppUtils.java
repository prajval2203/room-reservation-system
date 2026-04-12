package com.prajval.roomReservationSystem.util;

import com.prajval.roomReservationSystem.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {

    public static User getCurrentUser(){
        return
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
