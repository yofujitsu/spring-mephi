package com.yofujitsu.hotel_service.dto.hotel;

import java.util.UUID;

public record HotelStatsDto(
        UUID hotelId,
        long totalRooms,
        long availableRooms,
        long totalTimesBooked
) {
}
