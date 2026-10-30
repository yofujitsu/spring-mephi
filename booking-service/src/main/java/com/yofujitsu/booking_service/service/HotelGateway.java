package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.client.HotelClient;
import com.yofujitsu.booking_service.dto.hotel.ConfirmAvailabilityRequest;
import com.yofujitsu.booking_service.dto.hotel.ReleaseRequest;
import com.yofujitsu.booking_service.exception.HotelUnavailableException;
import com.yofujitsu.booking_service.exception.RoomNotAvailableException;
import com.yofujitsu.booking_service.exception.RoomNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelGateway {

    private final HotelClient hotelClient;

    @Retryable(
            retryFor = HotelUnavailableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 300, multiplier = 2.0)
    )
    public void confirm(UUID roomId, ConfirmAvailabilityRequest request) {
        try {
            hotelClient.confirmAvailability(roomId, request);
            log.info("Confirmed room availability: roomId={}, bookingId={}, requestId={}",
                    roomId, request.bookingId(), request.requestId());
        } catch (FeignException.Conflict exception) {
            throw new RoomNotAvailableException("Room is no longer available: " + roomId);
        } catch (FeignException.NotFound exception) {
            throw new RoomNotFoundException("Room not found: " + roomId);
        } catch (FeignException exception) {
            throw new HotelUnavailableException(
                    "Failed to confirm room availability: " + roomId, exception);
        }
    }

    @Retryable(
            retryFor = HotelUnavailableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 300, multiplier = 2.0)
    )
    public void release(UUID roomId, ReleaseRequest request) {
        try {
            hotelClient.release(roomId, request);
            log.info("Released room lock: roomId={}, bookingId={}, requestId={}",
                    roomId, request.bookingId(), request.requestId());
        } catch (FeignException exception) {
            throw new HotelUnavailableException(
                    "Failed to release room lock: " + roomId, exception);
        }
    }
}
