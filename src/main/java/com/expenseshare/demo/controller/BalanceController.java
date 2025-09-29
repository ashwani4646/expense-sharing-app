package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.BalanceUpdateResponse;
import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.GroupBalanceSummary;
import com.expenseshare.demo.services.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * Update balances after an expense is created
     */
    @PostMapping("/balances/update-from-expense")
    public ResponseEntity<BalanceUpdateResponse> updateBalancesFromExpense(
            @RequestBody ExpenseBalanceUpdateRequest request) {
        log.info("Updating balances from expense: {}", request);

        try {
            balanceService.updateBalancesForExpense(request);

            BalanceUpdateResponse response = BalanceUpdateResponse.builder()
                    .status("SUCCESS")
                    .message("Balances updated successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update balances from expense", e);

            BalanceUpdateResponse response = BalanceUpdateResponse.builder()
                    .status("ERROR")
                    .message("Failed to update balances: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all balances for a specific group
     */
    @GetMapping("/balances/group/{groupId}")
    public ResponseEntity<GroupBalanceSummary> getGroupBalances(@PathVariable Long groupId) {
        log.info("Fetching balances for group: {}", groupId);

        // This would need to be implemented in BalanceService
        // For now, returning a basic response structure

        GroupBalanceSummary summary = GroupBalanceSummary.builder()
                .groupId(groupId)
                .message("Feature to be implemented")
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Simplify balances for a group (remove small amounts)
     */
    @PostMapping("/balances/group/{groupId}/simplify")
    public ResponseEntity<BalanceUpdateResponse> simplifyGroupBalances(@PathVariable Long groupId) {
        log.info("Simplifying balances for group: {}", groupId);

        try {
            balanceService.simplifyBalances(groupId);

            BalanceUpdateResponse response = BalanceUpdateResponse.builder()
                    .status("SUCCESS")
                    .message("Group balances simplified successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to simplify group balances", e);

            BalanceUpdateResponse response = BalanceUpdateResponse.builder()
                    .status("ERROR")
                    .message("Failed to simplify balances: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
