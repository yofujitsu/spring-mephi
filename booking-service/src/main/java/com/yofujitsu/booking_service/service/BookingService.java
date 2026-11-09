package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.dto.booking.BookingDto;
import com.yofujitsu.booking_service.dto.booking.CreateBookingRequest;
import com.yofujitsu.booking_service.dto.common.PageResponse;
import com.yofujitsu.booking_service.exception.ConflictException;
import com.yofujitsu.booking_service.exception.HotelUnavailableException;
import com.yofujitsu.booking_service.exception.ResourceNotFoundException;
import com.yofujitsu.booking_service.exception.RoomNotAvailableException;
import com.yofujitsu.booking_service.exception.RoomNotFoundException;
import com.yofujitsu.booking_service.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingPersistence bookingPersistence;
    private final SagaService sagaService;

    public BookingDto create(UUID userId, CreateBookingRequest request) {
        var creation = bookingPersistence.createPending(userId, request);
        BookingDto pending = creation.booking();
        if (!creation.created()) {
            return pending;
        }

        try {
            sagaService.confirm(pending);
        } catch (RuntimeException exception) {
            cancelAndRelease(pending);
            throw translate(exception);
        }

        try {
            return bookingPersistence.markConfirmed(pending.id());
        } catch (RuntimeException exception) {
            cancelAndRelease(pending);
            throw exception;
        }
    }

    public PageResponse<BookingDto> getBookings(UUID userId, int page, int size) {
        return bookingPersistence.getBookings(userId, page, size);
    }

    public BookingDto getBooking(UUID id, UUID userId, boolean admin) {
        return bookingPersistence.getBooking(id, userId, admin);
    }

    public void cancel(UUID id, UUID userId, boolean admin) {
        BookingDto cancelled = bookingPersistence.cancel(id, userId, admin);
        releaseBestEffort(cancelled);
    }

    private void cancelAndRelease(BookingDto booking) {
        try {
            bookingPersistence.markCancelled(booking.id());
        } catch (RuntimeException exception) {
            log.error("Failed to cancel booking {} after saga failure",
                    booking.id(), exception);
        }
        releaseBestEffort(booking);
    }

    private void releaseBestEffort(BookingDto booking) {
        try {
            sagaService.release(booking);
            bookingPersistence.markReleased(booking.id());
        } catch (RuntimeException exception) {
            log.error("Failed to release room lock for booking {}", booking.id(), exception);
        }
    }

    @Scheduled(fixedDelayString = "${app.compensation.delay:10000}",
            initialDelayString = "${app.compensation.delay:10000}")
    public void retryCompensations() {
        bookingPersistence.pendingReleases().forEach(this::releaseBestEffort);
    }

    private RuntimeException translate(RuntimeException exception) {
        if (exception instanceof RoomNotAvailableException) {
            return new ConflictException("Room is no longer available");
        }
        if (exception instanceof RoomNotFoundException roomNotFound) {
            return new ResourceNotFoundException(roomNotFound.getMessage());
        }
        if (exception instanceof HotelUnavailableException) {
            return new ServiceUnavailableException(
                    "Hotel service is unavailable, booking was cancelled", exception);
        }
        return exception;
    }
}
