package com.yofujitsu.hotel_service.dto.hotel;

import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Hotel;

import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Hotel}
 */
public record HotelDto(UUID id, String name, String address, List<RoomDto> rooms) {
}