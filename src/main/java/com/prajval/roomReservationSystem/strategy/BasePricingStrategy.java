package com.prajval.roomReservationSystem.strategy;

import com.prajval.roomReservationSystem.entity.Inventory;
import java.math.BigDecimal;

public class BasePricingStrategy implements PricingStrategy{

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}
