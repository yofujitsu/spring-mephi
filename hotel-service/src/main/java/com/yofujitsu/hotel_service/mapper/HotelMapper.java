package com.yofujitsu.hotel_service.mapper;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    Hotel toEntity(HotelCreateDto hotelCreateDto);

    @Mapping(target = "rooms", source = "rooms")
    HotelDto toDto(Hotel hotel, List<RoomDto> rooms);
}
