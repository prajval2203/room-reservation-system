package com.prajval.roomReservationSystem.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {


    @Around("execution(* com.prajval.roomReservationSystem.service.impl.*.*(..))")
    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toShortString();
        log.info("String method: {}",  methodName);

        Object result = joinPoint.proceed();

        long durationTime = System.currentTimeMillis() - startTime;
        log.info("Completed Prajval's Method : {} in {}ms", methodName, durationTime );

        return result;
    }

}
