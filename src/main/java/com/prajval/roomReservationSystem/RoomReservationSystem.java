package com.prajval.roomReservationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoomReservationSystem {

	public static void main(String[] args) {
		SpringApplication.run(RoomReservationSystem.class, args);
	}

}
