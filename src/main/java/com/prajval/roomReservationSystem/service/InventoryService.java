package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room roomId);

    void deleteFutureInventories(Room room);
}
