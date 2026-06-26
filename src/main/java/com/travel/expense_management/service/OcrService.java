package com.travel.expense_management.service;

import com.travel.expense_management.dto.expense.OcrResult;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    OcrResult extractReceiptData(MultipartFile file);
}
