package com.travel.expense_management.controller;

import com.travel.expense_management.dto.dashboard.DashboardMetrics;
import com.travel.expense_management.dto.dashboard.ReportsSummary;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard metrics and summaries")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard metrics", description = "Retrieves aggregated expense dashboard metrics for the authenticated user")
    public ResponseEntity<DashboardMetrics> getMetrics(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(dashboardService.getMetrics(currentUser));
    }

    @GetMapping("/reports/summary")
    @Operation(summary = "Get reports summary", description = "Retrieves summary status and totals of expense reports")
    public ResponseEntity<ReportsSummary> getReportsSummary(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(dashboardService.getReportsSummary(currentUser));
    }

    @GetMapping("/reports/csv")
    @Operation(summary = "Download reports CSV", description = "Generates and exports a CSV file containing all expenses and trip records for the authenticated user")
    public ResponseEntity<String> getReportsCsv(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        String csvData = dashboardService.getReportsCsv(currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expense_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
