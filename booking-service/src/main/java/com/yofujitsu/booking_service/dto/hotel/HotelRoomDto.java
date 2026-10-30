package com.yofujitsu.booking_service.dto.hotel;

import java.util.UUID;

public record HotelRoomDto(
        UUID id,
        UUID hotelId,
        Integer number,
        String available,
        Integer timesBooked
) {
}
