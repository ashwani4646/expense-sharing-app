package com.expenseshare.demo.mapper;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.dto.SettlementDto;
import com.expenseshare.demo.entity.Expense;
import com.expenseshare.demo.entity.Settlement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseMapper INSTANCE = Mappers.getMapper(ExpenseMapper.class);
    Expense toEntity(ExpenseDto expenseDto);
    ExpenseDto toDTO(Expense expense);
}
