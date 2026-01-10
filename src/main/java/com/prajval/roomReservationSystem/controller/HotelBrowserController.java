package com.prajval.roomReservationSystem.controller;

import com.prajval.roomReservationSystem.dto.HotelDto;
import com.prajval.roomReservationSystem.dto.HotelInfoDto;
import com.prajval.roomReservationSystem.dto.HotelPriceDto;
import com.prajval.roomReservationSystem.dto.HotelSearchRequest;
import com.prajval.roomReservationSystem.service.HotelService;
import com.prajval.roomReservationSystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowserController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    private ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){

        var page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    private ResponseEntity<HotelInfoDto> getHotelInfoDto(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
