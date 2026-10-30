package com.yofujitsu.booking_service.mapper;

import com.yofujitsu.booking_service.dto.user.UserDto;
import com.yofujitsu.booking_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtos(List<User> users);
}
