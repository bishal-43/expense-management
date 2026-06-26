package com.travel.expense_management.dto.expense;

import com.travel.expense_management.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OcrResult(
        String description,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate date
) {}
