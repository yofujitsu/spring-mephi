package com.yofujitsu.booking_service.dto.hotel;

import java.util.UUID;

public record ReleaseRequest(
        UUID bookingId,
        UUID requestId
) {
}
