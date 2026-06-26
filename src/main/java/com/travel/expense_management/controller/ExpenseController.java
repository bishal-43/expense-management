package com.travel.expense_management.controller;

import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.dto.expense.ExpenseResponse;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long tripId,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ExpenseResponse response = expenseService.createExpense(tripId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trips/{tripId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpensesForTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<ExpenseResponse> response = expenseService.getExpensesForTrip(tripId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ExpenseResponse response = expenseService.getExpenseById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ExpenseResponse response = expenseService.updateExpense(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        expenseService.deleteExpense(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
