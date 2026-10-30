package com.yofujitsu.hotel_service.dto.room;

import com.yofujitsu.hotel_service.entity.Room;

import java.util.UUID;

public record RoomStatsDto(
        UUID id,
        UUID hotelId,
        Integer number,
        Room.AvailableStatus available,
        Integer timesBooked
) {
}
