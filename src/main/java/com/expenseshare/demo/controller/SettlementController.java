package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.SettleBalanceRequestDto;
import com.expenseshare.demo.dto.SettlementResponseDto;
import com.expenseshare.demo.dto.UserBalanceResponseDto;
import com.expenseshare.demo.entity.Settlement;
import com.expenseshare.demo.services.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/settlements/settle")
    public ResponseEntity<SettlementResponseDto> settleBalance(@RequestBody SettleBalanceRequestDto request) {
        log.info("Settlement request received: {}", request);
        SettlementResponseDto response = settlementService.settleBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/settlements/user/{userId}/balance")
    public ResponseEntity<UserBalanceResponseDto> getUserBalance(@PathVariable Long userId) {
        log.info("Fetching balance for user: {}", userId);
        UserBalanceResponseDto response = settlementService.getUserBalance(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settlements/user/{userId}")
    public ResponseEntity<List<SettlementResponseDto>> getUserSettlements(@PathVariable Long userId) {
        log.info("Fetching settlements for user: {}", userId);
        List<SettlementResponseDto> responses = settlementService.getUserSettlements(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/settlements/between/{userId1}/{userId2}")
    public ResponseEntity<List<SettlementResponseDto>> getSettlementsBetweenUsers(
            @PathVariable Long userId1, @PathVariable Long userId2) {
        log.info("Fetching settlements between users: {} and {}", userId1, userId2);
        List<Settlement> settlements = settlementService.findSettlementsBetweenUsers(userId1, userId2);
        List<SettlementResponseDto> responses = settlements.stream()
                .map(settlementService::mapToSettlementResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
