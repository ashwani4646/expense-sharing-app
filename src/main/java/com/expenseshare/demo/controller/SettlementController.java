package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.dto.SettlementDto;
import com.expenseshare.demo.services.GroupService;
import com.expenseshare.demo.services.SettlementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlements)")
public class SettlementController {
    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    ResponseEntity<SettlementDto> addSettlement(@RequestBody SettlementDto settlementDto){
        settlementService.addSettlement(settlementDto);
        return  new ResponseEntity<>(settlementDto, HttpStatus.OK);
    }
}
