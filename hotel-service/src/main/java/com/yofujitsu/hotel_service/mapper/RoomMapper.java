package com.yofujitsu.hotel_service.mapper;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.dto.room.RoomCreateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Hotel;
import com.yofujitsu.hotel_service.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public class RoomMapper {
    @Mapping(target = "", source = "")
    public Room toEntity(RoomCreateDto roomCreateDto) {
        return new Room();
    }

    @Mapping(target = "", source = "")
    public RoomDto toDto(Room room) {
        return new RoomDto(room.getId(), room.getHotelId(), room.getNumber(),
                room.getAvailable(), room.getTimesBooked());
    }
}
