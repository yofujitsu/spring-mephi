package com.yofujitsu.booking_service.dto.hotel;

import java.time.LocalDate;
import java.util.UUID;

public record ConfirmAvailabilityRequest(
        UUID bookingId,
        LocalDate startDate,
        LocalDate endDate,
        UUID requestId
) {
}
