package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.dto.*;
import com.prajval.roomReservationSystem.entity.Room;
import org.springframework.data.domain.Page;
import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room roomId);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
