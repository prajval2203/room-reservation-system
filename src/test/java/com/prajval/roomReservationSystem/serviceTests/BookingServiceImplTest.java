package com.prajval.roomReservationSystem.serviceTests;

import com.prajval.roomReservationSystem.dto.BookingDto;
import com.prajval.roomReservationSystem.dto.BookingRequestDto;
import com.prajval.roomReservationSystem.dto.HotelReportDto;
import com.prajval.roomReservationSystem.entity.*;
import com.prajval.roomReservationSystem.entity.enums.BookingStatus;
import com.prajval.roomReservationSystem.entity.enums.Gender;
import com.prajval.roomReservationSystem.exceptions.ResourceNotFoundException;
import com.prajval.roomReservationSystem.exceptions.UnAuthorizedException;
import com.prajval.roomReservationSystem.repository.*;
import com.prajval.roomReservationSystem.service.CheckOutService;
import com.prajval.roomReservationSystem.service.impl.BookingServiceImpl;
import com.prajval.roomReservationSystem.strategy.PricingService;
import com.prajval.roomReservationSystem.util.AppUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private CheckOutService checkOutService;
    @Mock
    private PricingService pricingService;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(10L)
                .name("Parth Kadam")
                .email("parth123@gmail.com")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2002, 12, 20))
                .password("ParthKadam@123")
                .build();
    }

    @Test
    void hasBookingExpired_whenExpired_thenReturnTrue() {
        Booking booking = new Booking();
        booking.setCreatedAt(LocalDateTime.now().minusMinutes(15));

        boolean result = bookingService.hasBookingExpired(booking);

        assertThat(result).isTrue();
    }

    @Test
    void hasBookingExpired_whenNotExpired_thenReturnFalse() {
        Booking booking = new Booking();
        booking.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        boolean result = bookingService.hasBookingExpired(booking);

        assertThat(result).isFalse();
    }

    @Test
    void initialiseBooking_whenHotelNotFound_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setHotelId(1L);
        requestDto.setRoomId(1L);
        requestDto.setCheckInDate(LocalDate.now());
        requestDto.setCheckOutDate(LocalDate.now().plusDays(2));
        requestDto.setRoomsCount(1);

        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.initialiseBooking(requestDto));
    }

    @Test
    void initialiseBooking_whenRoomNotAvailable_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setHotelId(1L);
        requestDto.setRoomId(1L);
        requestDto.setCheckInDate(LocalDate.now());
        requestDto.setCheckOutDate(LocalDate.now().plusDays(2));
        requestDto.setRoomsCount(1);

        Hotel mockHotel = new Hotel();
        Room mockRoom = new Room();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));
        when(inventoryRepository.findAndLockAvailableInventory(any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> bookingService.initialiseBooking(requestDto));
    }

    @Test
    void initialiseBooking_whenSuccess_thenReturnBookingDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setHotelId(1L);
        requestDto.setRoomId(1L);
        requestDto.setCheckInDate(LocalDate.now());
        requestDto.setCheckOutDate(LocalDate.now());
        requestDto.setRoomsCount(1);

        Hotel mockHotel = new Hotel();
        Room mockRoom = new Room();
        mockRoom.setId(1L);

        Inventory inventory = new Inventory();
        List<Inventory> inventoryList = List.of(inventory); // size 1 = daysCount 1

        Booking mockBooking = new Booking();
        BookingDto mockBookingDto = new BookingDto();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));
        when(inventoryRepository.findAndLockAvailableInventory(any(), any(), any(), anyInt()))
                .thenReturn(inventoryList);
        when(pricingService.calculateTotalPrice(inventoryList))
                .thenReturn(BigDecimal.valueOf(1000));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        when(modelMapper.map(mockBooking, BookingDto.class)).thenReturn(mockBookingDto);

        try (MockedStatic<AppUtils> mockedStatic = mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getCurrentUser).thenReturn(mockUser);

            BookingDto result = bookingService.initialiseBooking(requestDto);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
            verify(inventoryRepository).initBooking(any(), any(), any(), anyInt());
        }
    }


    @Test
    void getBookingStatus_whenValidUser_thenReturnStatus() {
        Booking mockBooking = new Booking();
        mockBooking.setUser(mockUser);
        mockBooking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        try (MockedStatic<AppUtils> mockedStatic = mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getCurrentUser).thenReturn(mockUser);

            String status = bookingService.getBookingStatus(1L);

            assertThat(status).isEqualTo("CONFIRMED");
        }
    }

    @Test
    void getBookingStatus_whenBookingNotFound_thenThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingStatus(1L));
    }

    @Test
    void getBookingStatus_whenWrongUser_thenThrowException() {
        User differentUser = User.builder().id(99L).build();

        Booking mockBooking = new Booking();
        mockBooking.setUser(differentUser);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        try (MockedStatic<AppUtils> mockedStatic = mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getCurrentUser).thenReturn(mockUser);

            assertThrows(UnAuthorizedException.class,
                    () -> bookingService.getBookingStatus(1L));
        }
    }

    @Test
    void getHotelReport_whenSuccess_thenReturnCorrectRevenue() {
        Hotel mockHotel = new Hotel();
        mockHotel.setOwner(mockUser);

        Booking confirmedBooking = new Booking();
        confirmedBooking.setBookingStatus(BookingStatus.CONFIRMED);
        confirmedBooking.setAmount(BigDecimal.valueOf(1000));

        Booking cancelledBooking = new Booking();
        cancelledBooking.setBookingStatus(BookingStatus.CANCELLED);
        cancelledBooking.setAmount(BigDecimal.valueOf(500));

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));
        when(bookingRepository.findByHotelAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(confirmedBooking, cancelledBooking));

        try (MockedStatic<AppUtils> mockedStatic = mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getCurrentUser).thenReturn(mockUser);

            HotelReportDto report = bookingService.getHotelReport(1L,
                    LocalDate.now().minusDays(7), LocalDate.now());

            assertThat(report.getBookingCount()).isEqualTo(1L);
            assertThat(report.getTotalRevenue())
                    .isEqualByComparingTo(BigDecimal.valueOf(1000));
        }
    }

    @Test
    void getHotelReport_whenNotOwner_thenThrowException() {
        User differentUser = User.builder().id(99L).build();
        Hotel mockHotel = new Hotel();
        mockHotel.setOwner(differentUser);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));

        try (MockedStatic<AppUtils> mockedStatic = mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getCurrentUser).thenReturn(mockUser);

            assertThrows(AccessDeniedException.class,
                    () -> bookingService.getHotelReport(1L,
                            LocalDate.now().minusDays(7), LocalDate.now()));
        }
    }
}