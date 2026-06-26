package com.travel.expense_management.service;

import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.dto.expense.ExpenseResponse;
import com.travel.expense_management.security.UserPrincipal;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(Long tripId, ExpenseRequest request, UserPrincipal currentUser);

    List<ExpenseResponse> getExpensesForTrip(Long tripId, UserPrincipal currentUser);

    ExpenseResponse getExpenseById(Long id, UserPrincipal currentUser);

    ExpenseResponse updateExpense(Long id, ExpenseRequest request, UserPrincipal currentUser);

    void deleteExpense(Long id, UserPrincipal currentUser);
}
