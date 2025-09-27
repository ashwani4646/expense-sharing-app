package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.SettlementDto;
import com.expenseshare.demo.mapper.SettlementMapper;
import com.expenseshare.demo.repository.SettlementRepository;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {
    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    public void addSettlement(SettlementDto settlementDto) {
       this.settlementRepository.save(SettlementMapper.INSTANCE.toEntity(settlementDto));
    }
}
