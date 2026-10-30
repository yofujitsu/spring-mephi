package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.dto.booking.BookingDto;
import com.yofujitsu.booking_service.dto.hotel.ConfirmAvailabilityRequest;
import com.yofujitsu.booking_service.dto.hotel.ReleaseRequest;
import com.yofujitsu.booking_service.exception.HotelUnavailableException;
import com.yofujitsu.booking_service.exception.RoomNotAvailableException;
import com.yofujitsu.booking_service.exception.RoomNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaService {

    private final HotelGateway hotelGateway;

    @CircuitBreaker(name = "hotelService", fallbackMethod = "confirmFallback")
    public void confirm(BookingDto booking) {
        ConfirmAvailabilityRequest request = new ConfirmAvailabilityRequest(
                booking.id(),
                booking.startDate(),
                booking.endDate(),
                UUID.randomUUID()
        );
        hotelGateway.confirm(booking.roomId(), request);
    }

    @CircuitBreaker(name = "hotelService", fallbackMethod = "releaseFallback")
    public void release(BookingDto booking) {
        ReleaseRequest request = new ReleaseRequest(booking.id(), UUID.randomUUID());
        hotelGateway.release(booking.roomId(), request);
    }

    public void confirmFallback(BookingDto booking, Throwable throwable) {
        if (throwable instanceof RoomNotAvailableException exception) {
            throw exception;
        }
        if (throwable instanceof RoomNotFoundException exception) {
            throw exception;
        }
        log.error("Hotel confirmation failed for booking {}: {}",
                booking.id(), throwable.getMessage());
        throw new HotelUnavailableException(
                "Hotel service is unavailable while confirming booking " + booking.id(),
                throwable);
    }

    public void releaseFallback(BookingDto booking, Throwable throwable) {
        log.error("Hotel release failed for booking {}: {}",
                booking.id(), throwable.getMessage());
        throw new HotelUnavailableException(
                "Hotel service is unavailable while releasing booking " + booking.id(),
                throwable);
    }
}
