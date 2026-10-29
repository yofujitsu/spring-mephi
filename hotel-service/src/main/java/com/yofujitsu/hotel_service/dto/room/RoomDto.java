package com.yofujitsu.hotel_service.dto.room;

import com.yofujitsu.hotel_service.entity.Room;

import java.util.UUID;

/**
 * DTO for {@link com.yofujitsu.hotel_service.entity.Room}
 */
public record RoomDto(UUID id, UUID hotelId, Integer number, Room.AvailableStatus available, Integer timesBooked) {
}