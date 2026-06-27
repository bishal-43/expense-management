package com.travel.expense_management.service;

import org.springframework.web.multipart.MultipartFile;

public interface ReceiptStorageService {

    String storeFile(MultipartFile file);

    byte[] loadFile(String filePath);

    void deleteFile(String filePath);
}
