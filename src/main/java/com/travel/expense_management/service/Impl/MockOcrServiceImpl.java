package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.expense.OcrResult;
import com.travel.expense_management.entity.ExpenseCategory;
import com.travel.expense_management.service.OcrService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MockOcrServiceImpl implements OcrService {

    private static final String TESSDATA_URL = "https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/eng.traineddata";
    private File tessdataDir;

    @PostConstruct
    public void init() {
        try {
            tessdataDir = new File("tessdata").getAbsoluteFile();
            if (!tessdataDir.exists()) {
                tessdataDir.mkdirs();
            }
            File trainedDataFile = new File(tessdataDir, "eng.traineddata");
            if (!trainedDataFile.exists()) {
                log.info("Downloading Tesseract English training data from {}...", TESSDATA_URL);
                URL url = URI.create(TESSDATA_URL).toURL();
                java.net.URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, trainedDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                log.info("Tesseract English training data downloaded successfully to {}", trainedDataFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to initialize or download Tesseract training data. Fallbacks will be used.", e);
        }
    }

    @Override
    public OcrResult extractReceiptData(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "";
        }

        String ocrText = null;
        try {
            File trainedDataFile = new File(tessdataDir, "eng.traineddata");
            if (trainedDataFile.exists()) {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(tessdataDir.getAbsolutePath());
                tesseract.setLanguage("eng");

                try (InputStream is = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(is);
                    if (image != null) {
                        log.info("Running real OCR on receipt: {}", filename);
                        ocrText = tesseract.doOCR(image);
                        log.info("Real OCR extraction completed successfully.");
                    } else {
                        log.warn("Uploaded file is not a valid image. Skipping real OCR.");
                    }
                }
            } else {
                log.warn("Tesseract training data file is missing. Skipping real OCR.");
            }
        } catch (Exception e) {
            log.error("Error occurred during Tesseract OCR execution: {}", e.getMessage());
        }

        if (ocrText != null && !ocrText.strip().isEmpty()) {
            return parseOcrText(ocrText, filename);
        } else {
            log.info("Using filename-based mock OCR fallback for: {}", filename);
            return fallbackHeuristics(filename);
        }
    }

    private OcrResult parseOcrText(String ocrText, String filename) {
        BigDecimal amount = parseAmount(ocrText);
        LocalDate date = parseDate(ocrText);
        ExpenseCategory category = parseCategory(ocrText, filename);
        String description = parseDescription(ocrText, filename);

        // If parsed components are incomplete, enrich them using fallback heuristics
        if (amount == null) {
            amount = fallbackHeuristics(filename).amount();
        }
        if (category == ExpenseCategory.OTHER) {
            category = fallbackHeuristics(filename).category();
        }

        return new OcrResult(description, amount, category, date != null ? date : LocalDate.now());
    }

    private BigDecimal parseAmount(String ocrText) {
        // Pattern matches "total", "amount", "due", "sum", "paid", etc. followed by numbers
        Pattern totalPattern = Pattern.compile("(?i)(?:total|amount|due|sum|net|paid|charge)[:\\s]*\\$?[:\\s]*([\\d,]+\\.\\d{2})");
        BigDecimal maxAmount = null;

        String[] lines = ocrText.split("\\r?\\n");
        for (String line : lines) {
            Matcher matcher = totalPattern.matcher(line);
            if (matcher.find()) {
                try {
                    String valueStr = matcher.group(1).replace(",", "");
                    BigDecimal val = new BigDecimal(valueStr);
                    if (maxAmount == null || val.compareTo(maxAmount) > 0) {
                        maxAmount = val;
                    }
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
        }

        // Generic decimal number pattern search if no keyword pattern matched
        if (maxAmount == null) {
            Pattern genericPattern = Pattern.compile("\\b(\\d+\\.\\d{2})\\b");
            for (String line : lines) {
                Matcher matcher = genericPattern.matcher(line);
                if (matcher.find()) {
                    try {
                        BigDecimal val = new BigDecimal(matcher.group(1));
                        if (maxAmount == null || val.compareTo(maxAmount) > 0) {
                            maxAmount = val;
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }

        return maxAmount;
    }

    private LocalDate parseDate(String ocrText) {
        // YYYY-MM-DD or YYYY/MM/DD
        Pattern datePattern1 = Pattern.compile("\\b(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})\\b");
        // DD/MM/YYYY or MM/DD/YYYY
        Pattern datePattern2 = Pattern.compile("\\b(\\d{1,2})[-/.](\\d{1,2})[-/.](\\d{4})\\b");

        String[] lines = ocrText.split("\\r?\\n");
        for (String line : lines) {
            Matcher m1 = datePattern1.matcher(line);
            if (m1.find()) {
                try {
                    int year = Integer.parseInt(m1.group(1));
                    int month = Integer.parseInt(m1.group(2));
                    int day = Integer.parseInt(m1.group(3));
                    return LocalDate.of(year, month, day);
                } catch (Exception e) {
                    // Ignore
                }
            }

            Matcher m2 = datePattern2.matcher(line);
            if (m2.find()) {
                try {
                    int part1 = Integer.parseInt(m2.group(1));
                    int part2 = Integer.parseInt(m2.group(2));
                    int year = Integer.parseInt(m2.group(3));

                    int month = part1;
                    int day = part2;
                    if (part1 > 12) {
                        day = part1;
                        month = part2;
                    } else if (part2 > 12) {
                        day = part2;
                        month = part1;
                    }

                    if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                        return LocalDate.of(year, month, day);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return null;
    }

    private ExpenseCategory parseCategory(String ocrText, String filename) {
        String combined = (ocrText + " " + filename).toLowerCase();

        if (combined.contains("starbucks") || combined.contains("coffee") || combined.contains("cafe")
                || combined.contains("restaurant") || combined.contains("food") || combined.contains("eat")
                || combined.contains("diner") || combined.contains("lunch") || combined.contains("dinner")
                || combined.contains("breakfast") || combined.contains("burger") || combined.contains("pizza")
                || combined.contains("mcdonald") || combined.contains("subway")) {
            return ExpenseCategory.FOOD;
        }

        if (combined.contains("uber") || combined.contains("lyft") || combined.contains("taxi")
                || combined.contains("cab") || combined.contains("flight") || combined.contains("delta")
                || combined.contains("airline") || combined.contains("railway") || combined.contains("train")
                || combined.contains("metro") || combined.contains("bus") || combined.contains("transport")
                || combined.contains("ride") || combined.contains("travel")) {
            return ExpenseCategory.TRANSPORT;
        }

        if (combined.contains("hotel") || combined.contains("motel") || combined.contains("hostel")
                || combined.contains("inn") || combined.contains("hilton") || combined.contains("marriott")
                || combined.contains("lodging") || combined.contains("stay") || combined.contains("room")
                || combined.contains("airbnb")) {
            return ExpenseCategory.LODGING;
        }

        return ExpenseCategory.OTHER;
    }

    private String parseDescription(String ocrText, String filename) {
        String[] lines = ocrText.split("\\r?\\n");
        for (String line : lines) {
            String cleanLine = line.trim();
            if (!cleanLine.isEmpty() && cleanLine.length() < 100) {
                return cleanLine;
            }
        }

        if (filename != null && !filename.isBlank()) {
            int lastDot = filename.lastIndexOf('.');
            String name = (lastDot > 0) ? filename.substring(0, lastDot) : filename;
            return "Receipt: " + name;
        }

        return "Receipt: Unknown";
    }

    private OcrResult fallbackHeuristics(String filename) {
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
