package com.yofujitsu.hotel_service.mapper;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public class HotelMapper {

    @Mapping(target = "", source = "")
    public Hotel toEntity(HotelCreateDto hotelCreateDto) {
        return new Hotel();
    }

    @Mapping(target = "", source = "")
    public HotelDto toDto(Hotel hotel) {
        return new HotelDto(hotel.getId(), hotel.getName(), hotel.getAddress());
    }
}
