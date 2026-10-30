package com.yofujitsu.booking_service.dto.booking;

import com.yofujitsu.booking_service.entity.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingDto(
        UUID id,
        UUID userId,
        UUID roomId,
        UUID hotelId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Instant createdAt
) {
}
