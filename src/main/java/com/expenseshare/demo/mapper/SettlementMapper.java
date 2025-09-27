package com.expenseshare.demo.mapper;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.dto.SettlementDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.Settlement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SettlementMapper {
    SettlementMapper INSTANCE = Mappers.getMapper(SettlementMapper.class);
    Settlement toEntity(SettlementDto settlementDto);
    SettlementDto toDTO(Settlement settlement);
}
