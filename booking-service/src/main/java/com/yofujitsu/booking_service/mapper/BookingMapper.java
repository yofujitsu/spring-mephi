package com.yofujitsu.booking_service.mapper;

import com.yofujitsu.booking_service.dto.booking.BookingDto;
import com.yofujitsu.booking_service.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {

    BookingDto toDto(Booking booking);
}
