package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.dto.expense.ExpenseResponse;
import com.travel.expense_management.entity.Expense;
import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.entity.TripStatus;
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
import org.springframework.web.multipart.MultipartFile;
import com.travel.expense_management.dto.expense.OcrResult;
import com.travel.expense_management.entity.Receipt;
import com.travel.expense_management.service.OcrService;
import com.travel.expense_management.service.ReceiptStorageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final AuthorizationService authorizationService;
    private final ReceiptStorageService receiptStorageService;
    private final OcrService ocrService;

    @Override
    public ExpenseResponse createExpense(Long tripId, ExpenseRequest request, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        authorizationService.authorizeTripAccess(trip, currentUser);

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot add expense to a trip that is already " + trip.getStatus());
        }
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

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot update expense for a trip that is already " + trip.getStatus());
        }
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

        if (expense.getTrip().getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot delete expense for a trip that is already " + expense.getTrip().getStatus());
        }

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

    @Override
    public ExpenseResponse uploadReceipt(Long expenseId, MultipartFile file, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        if (expense.getTrip().getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot upload receipt for a trip that is already " + expense.getTrip().getStatus());
        }

        // Store new file first (runs validations)
        String uniqueFilename = receiptStorageService.storeFile(file);

        // If storing the new file succeeded, we can safely delete the old physical file
        if (expense.getReceipt() != null) {
            receiptStorageService.deleteFile(expense.getReceipt().getFilePath());
        }

        // Build Receipt entity
        Receipt receipt = Receipt.builder()
                .fileName(file.getOriginalFilename())
                .filePath(uniqueFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        expense.setReceipt(receipt);

        // Trigger OCR analysis to autofill/update expense fields
        try {
            OcrResult ocrResult = ocrService.extractReceiptData(file);
            if (ocrResult != null) {
                if (ocrResult.description() != null && !ocrResult.description().isBlank()) {
                    expense.setDescription(ocrResult.description());
                }
                if (ocrResult.amount() != null) {
                    expense.setAmount(ocrResult.amount());
                }
                if (ocrResult.category() != null) {
                    expense.setCategory(ocrResult.category());
                }
                if (ocrResult.date() != null) {
                    if (!ocrResult.date().isBefore(expense.getTrip().getStartDate()) &&
                        !ocrResult.date().isAfter(expense.getTrip().getEndDate())) {
                        expense.setDate(ocrResult.date());
                    }
                }
            }
        } catch (Exception e) {
            // Suppress OCR errors for resiliency
        }

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getReceiptFile(Long expenseId, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        if (expense.getReceipt() == null) {
            throw new BadRequestException("Expense with ID " + expenseId + " does not have a receipt.");
        }

        return receiptStorageService.loadFile(expense.getReceipt().getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String getReceiptContentType(Long expenseId, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        if (expense.getReceipt() == null) {
            throw new BadRequestException("Expense with ID " + expenseId + " does not have a receipt.");
        }

        return expense.getReceipt().getContentType();
    }

    @Override
    public ExpenseResponse deleteReceipt(Long expenseId, UserPrincipal currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        authorizationService.authorizeTripAccess(expense.getTrip(), currentUser);

        if (expense.getTrip().getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot delete receipt for a trip that is already " + expense.getTrip().getStatus());
        }

        if (expense.getReceipt() == null) {
            throw new BadRequestException("Expense with ID " + expenseId + " does not have a receipt.");
        }

        receiptStorageService.deleteFile(expense.getReceipt().getFilePath());
        expense.setReceipt(null);

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(savedExpense);
    }
}
