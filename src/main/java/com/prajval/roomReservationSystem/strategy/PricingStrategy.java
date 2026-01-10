package com.prajval.roomReservationSystem.strategy;

import com.prajval.roomReservationSystem.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
