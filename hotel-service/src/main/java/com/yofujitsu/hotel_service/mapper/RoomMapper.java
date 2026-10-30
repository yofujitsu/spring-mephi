package com.yofujitsu.hotel_service.mapper;

import com.yofujitsu.hotel_service.dto.room.RoomCreateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timesBooked", ignore = true)
    Room toEntity(RoomCreateDto roomCreateDto);

    RoomDto toDto(Room room);

    List<RoomDto> toDtos(List<Room> rooms);
}
