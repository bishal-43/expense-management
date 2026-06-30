package com.travel.expense_management.dto.dashboard;

import java.math.BigDecimal;
import java.util.Map;

public record ReportsSummary(
        Map<String, BigDecimal> categorySpend,
        Map<String, BigDecimal> tripSpend
) {}
