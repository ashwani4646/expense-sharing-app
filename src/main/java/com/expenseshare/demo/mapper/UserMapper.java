package com.expenseshare.demo.mapper;

import com.expenseshare.demo.dto.UserDto;
import com.expenseshare.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDTO(User user);

    User toEntity(UserDto userDto);
}
