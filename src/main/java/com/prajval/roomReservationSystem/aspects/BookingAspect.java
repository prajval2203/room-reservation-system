package com.prajval.roomReservationSystem.aspects;

import com.prajval.roomReservationSystem.entity.User;
import com.prajval.roomReservationSystem.util.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class BookingAspect {

    @Before("execution(* com.prajval.roomReservationSystem.service.impl.BookingServiceImpl.*(..))")
    public void performBookingOperation(JoinPoint joinPoint){

        try {
            User currentUser = AppUtils.getCurrentUser();
            String methodName = joinPoint.getSignature().getName();

            log.info("Booking operation [{}] called by user: {} (id: {})",
                    methodName, currentUser.getEmail(), currentUser.getId());
        }
        catch (Exception ex){
            log.warn("Booking operation [{}] called by unauthenticated context",
                    joinPoint.getSignature().getName());
        }
    }
}
