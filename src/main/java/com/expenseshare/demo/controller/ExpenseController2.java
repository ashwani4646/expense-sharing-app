package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.services.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expenses")
public class ExpenseController2 {
//
//    private final ExpenseService expenseService;
//
//    public ExpenseController2(@Autowired ExpenseService expenseService){
//        this.expenseService = expenseService;
//    }
//
//    @PostMapping
//    ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseDto expense){
//        expenseService.addExpense(expense);
//        return  new ResponseEntity<>(expense, HttpStatus.OK);
//    }
//
//    @PutMapping
//    ResponseEntity<ExpenseDto> modifyExpense(@RequestBody ExpenseDto expense){
//        return  new ResponseEntity<>(expense, HttpStatus.OK);
//    }
//
//    @DeleteMapping("/{id}")
//    ResponseEntity<Void> deleteGroup(@PathVariable Long id){
//        return  new ResponseEntity<>(HttpStatus.OK);
//    }
}
