package com.prajval.roomReservationSystem.service;

import com.prajval.roomReservationSystem.dto.HotelDto;
import com.prajval.roomReservationSystem.dto.HotelPriceDto;
import com.prajval.roomReservationSystem.dto.HotelSearchRequest;
import com.prajval.roomReservationSystem.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room roomId);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
