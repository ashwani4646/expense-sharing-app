package com.expenseshare.demo.mapper;
import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);
    Group toEntity(GroupDto groupDto);
    GroupDto toDTO(Group group);
}
