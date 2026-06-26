package com.travel.expense_management.dto.expense;

import com.travel.expense_management.entity.Expense;
import com.travel.expense_management.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate date,
        Long tripId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getTrip().getId(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
