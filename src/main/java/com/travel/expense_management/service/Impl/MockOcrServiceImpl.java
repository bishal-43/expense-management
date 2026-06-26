package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.expense.OcrResult;
import com.travel.expense_management.entity.ExpenseCategory;
import com.travel.expense_management.service.OcrService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class MockOcrServiceImpl implements OcrService {

    @Override
    public OcrResult extractReceiptData(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "";
        }
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.contains("starbucks")) {
            return new OcrResult(
                    "Starbucks Coffee",
                    new BigDecimal("12.50"),
                    ExpenseCategory.FOOD,
                    LocalDate.now()
            );
        } else if (lowerFilename.contains("uber")) {
            return new OcrResult(
                    "Uber Ride",
                    new BigDecimal("24.75"),
                    ExpenseCategory.TRANSPORT,
                    LocalDate.now()
            );
        } else if (lowerFilename.contains("hotel") || lowerFilename.contains("hilton")) {
            return new OcrResult(
                    "Hilton Hotel",
                    new BigDecimal("189.99"),
                    ExpenseCategory.LODGING,
                    LocalDate.now()
            );
        } else if (lowerFilename.contains("flight") || lowerFilename.contains("delta")) {
            return new OcrResult(
                    "Delta Flight",
                    new BigDecimal("350.00"),
                    ExpenseCategory.TRANSPORT,
                    LocalDate.now()
            );
        } else {
            return new OcrResult(
                    "Receipt: " + (filename.isEmpty() ? "Unknown" : filename),
                    new BigDecimal("45.00"),
                    ExpenseCategory.OTHER,
                    LocalDate.now()
            );
        }
    }
}
