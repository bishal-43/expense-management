package com.travel.expense_management.dto.dashboard;

import java.math.BigDecimal;

public record DashboardMetrics(
        String role,
        
        // Employee Metrics
        Long totalTrips,
        BigDecimal totalSpent,
        BigDecimal totalBudget,
        Double budgetUtilizationPercentage,
        Long pendingTripsCount,
        Long approvedTripsCount,
        Long rejectedTripsCount,

        // Manager Metrics
        Long totalTripsCount,
        Long totalUsersCount,
        Long pendingApprovalsCount
) {}
