package com.prajval.roomReservationSystem.service.impl;

import com.prajval.roomReservationSystem.dto.BookingDto;
import com.prajval.roomReservationSystem.dto.BookingRequestDto;
import com.prajval.roomReservationSystem.dto.GuestDto;
import com.prajval.roomReservationSystem.entity.*;
import com.prajval.roomReservationSystem.entity.enums.BookingStatus;
import com.prajval.roomReservationSystem.exceptions.ResourceNotFoundException;
import com.prajval.roomReservationSystem.exceptions.UnAuthorizedException;
import com.prajval.roomReservationSystem.repository.*;
import com.prajval.roomReservationSystem.service.BookingService;
import com.prajval.roomReservationSystem.service.CheckOutService;
import com.prajval.roomReservationSystem.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckOutService checkOutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;


    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequestDto bookingRequestDto) {

        log.info("Initializing booking for hotel : {}, room: {}, date: {}-{}", bookingRequestDto.getHotelId(),
                bookingRequestDto.getRoomId(), bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id: " +bookingRequestDto.getHotelId()));

        Room room = roomRepository.findById(bookingRequestDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id: " +bookingRequestDto.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate(), bookingRequestDto.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate()) + 1;
        if(inventoryList.size() != daysCount){
            throw  new IllegalStateException("Room is not available anymore ");
        }


        inventoryRepository.initBooking(room.getId(), bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate(), bookingRequestDto.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDto.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getCurrentUser())
                .roomCount(bookingRequestDto.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with Id: " +bookingId));
        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        if (booking.getBookingStatus() != BookingStatus.RESERVED &&
                booking.getBookingStatus() != BookingStatus.GUESTS_ADDED){
            throw new IllegalStateException("Booking is not under the Reserved/Confirmed state, cannot add the guests");
        }
        for (GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkOutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success",
                frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }

    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (booking.getBookingStatus() !=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed booking can be cancelled: ");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);


        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomCount());



        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        return booking.getBookingStatus().name();
    }


    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        return
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
