package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.dto.expense.ExpenseResponse;
import com.travel.expense_management.entity.Expense;
import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.exception.BadRequestException;
import com.travel.expense_management.exception.ResourceNotFoundException;
import com.travel.expense_management.repository.ExpenseRepository;
import com.travel.expense_management.repository.TripRepository;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.AuthorizationService;
import com.travel.expense_management.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final AuthorizationService authorizationService;

    @Override
    public ExpenseResponse createExpense(Long tripId, ExpenseRequest request, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        authorizationService.authorizeTripAccess(trip, currentUser);
        validateExpenseDate(request, trip);

        Expense expense = Expense.builder()
                .description(request.description())
                .amount(request.amount())
                .category(request.category())
                .date(request.date())
                .trip(trip)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesForTrip(Long tripId, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        authorizationService.authorizeTripAccess(trip, currentUser);

        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        return expenses.stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        return ExpenseResponse.from(expense);
    }

    @Override
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        Trip trip = expense.getTrip();
        authorizationService.authorizeTripAccess(trip, currentUser);
        validateExpenseDate(request, trip);

        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.date());

        Expense updatedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        expenseRepository.delete(expense);
    }

    private void validateExpenseDate(ExpenseRequest request, Trip trip) {
        if (request.date().isBefore(trip.getStartDate()) || request.date().isAfter(trip.getEndDate())) {
            throw new BadRequestException(String.format(
                    "Expense date %s must be within the trip duration (%s to %s).",
                    request.date(), trip.getStartDate(), trip.getEndDate()
            ));
        }
    }
}
