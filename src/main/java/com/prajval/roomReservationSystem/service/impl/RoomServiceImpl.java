package com.prajval.roomReservationSystem.service.impl;

import com.prajval.roomReservationSystem.dto.RoomDto;
import com.prajval.roomReservationSystem.entity.Hotel;
import com.prajval.roomReservationSystem.entity.Room;
import com.prajval.roomReservationSystem.entity.User;
import com.prajval.roomReservationSystem.exceptions.ResourceNotFoundException;
import com.prajval.roomReservationSystem.exceptions.UnAuthorizedException;
import com.prajval.roomReservationSystem.repository.HotelRepository;
import com.prajval.roomReservationSystem.repository.RoomRepository;
import com.prajval.roomReservationSystem.service.InventoryService;
import com.prajval.roomReservationSystem.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import static com.prajval.roomReservationSystem.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;


    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new Room with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with Id" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: " +hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {

        log.info("Getting all Rooms with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with Id" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: " +hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting Room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room Not Found with Id" + roomId));

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting Room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room Not Found with Id" + roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This user does not own this room with id: " +roomId);
        }

        roomRepository.deleteById(roomId);
        inventoryService.deleteAllInventories(room);
        // todo: delete all the future inventory for this.
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {

        log.info("Updating Room with ID: {}", roomId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with Id" + hotelId));

        User user = getCurrentUser();
        if (!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id: " +hotelId);
        }
        Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Room Not Found with Id" + roomId));

        modelMapper.map(roomDto, room);
        room.setId(roomId);
        room = roomRepository.save(room);

        return modelMapper.map(room, RoomDto.class);
    }
}
