package com.travel.expense_management.service;

import com.travel.expense_management.dto.dashboard.DashboardMetrics;
import com.travel.expense_management.dto.dashboard.ReportsSummary;
import com.travel.expense_management.security.UserPrincipal;

public interface DashboardService {
    DashboardMetrics getMetrics(UserPrincipal currentUser);
    ReportsSummary getReportsSummary(UserPrincipal currentUser);
    String getReportsCsv(UserPrincipal currentUser);
}
