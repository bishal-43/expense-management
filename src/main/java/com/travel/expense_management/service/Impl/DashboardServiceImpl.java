package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.dashboard.DashboardMetrics;
import com.travel.expense_management.dto.dashboard.ReportsSummary;
import com.travel.expense_management.entity.Expense;
import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.entity.TripStatus;
import com.travel.expense_management.repository.ExpenseRepository;
import com.travel.expense_management.repository.TripRepository;
import com.travel.expense_management.repository.UserRepository;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardMetrics getMetrics(UserPrincipal currentUser) {
        String roleStr = currentUser.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_Employee");

        boolean isManagerOrAdmin = roleStr.equals("ROLE_Manager") || roleStr.equals("ROLE_Admin");

        if (isManagerOrAdmin) {
            long totalTripsCount = tripRepository.count();
            long totalUsersCount = userRepository.count();
            long pendingApprovalsCount = tripRepository.findByStatus(TripStatus.PENDING).size();

            BigDecimal totalSpent = expenseRepository.findAll().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalBudget = tripRepository.findAll().stream()
                    .map(Trip::getBudget)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Double budgetUtilization = 0.0;
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                budgetUtilization = totalSpent.multiply(new BigDecimal("100"))
                        .divide(totalBudget, 2, java.math.RoundingMode.HALF_UP)
                        .doubleValue();
            }

            return new DashboardMetrics(
                    roleStr,
                    null, totalSpent, totalBudget, budgetUtilization, null, null, null,
                    totalTripsCount, totalUsersCount, pendingApprovalsCount
            );
        } else {
            Long userId = currentUser.getId();
            List<Trip> myTrips = tripRepository.findByUserId(userId);
            long totalTrips = myTrips.size();
            long pendingTripsCount = myTrips.stream().filter(t -> t.getStatus() == TripStatus.PENDING).count();
            long approvedTripsCount = myTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count();
            long rejectedTripsCount = myTrips.stream().filter(t -> t.getStatus() == TripStatus.REJECTED).count();

            List<Expense> myExpenses = expenseRepository.findAll().stream()
                    .filter(e -> e.getTrip().getUser().getId().equals(userId))
                    .collect(Collectors.toList());

            BigDecimal totalSpent = myExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalBudget = myTrips.stream()
                    .map(Trip::getBudget)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Double budgetUtilization = 0.0;
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                budgetUtilization = totalSpent.multiply(new BigDecimal("100"))
                        .divide(totalBudget, 2, java.math.RoundingMode.HALF_UP)
                        .doubleValue();
            }

            return new DashboardMetrics(
                    roleStr,
                    totalTrips, totalSpent, totalBudget, budgetUtilization,
                    pendingTripsCount, approvedTripsCount, rejectedTripsCount,
                    null, null, null
            );
        }
    }

    @Override
    public ReportsSummary getReportsSummary(UserPrincipal currentUser) {
        String roleStr = currentUser.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_Employee");

        boolean isManagerOrAdmin = roleStr.equals("ROLE_Manager") || roleStr.equals("ROLE_Admin");

        List<Expense> expenses;
        if (isManagerOrAdmin) {
            expenses = expenseRepository.findAll();
        } else {
            expenses = expenseRepository.findAll().stream()
                    .filter(e -> e.getTrip().getUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        Map<String, BigDecimal> categorySpend = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().name(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> tripSpend = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTrip().getDestination() + " (ID: " + e.getTrip().getId() + ")",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return new ReportsSummary(categorySpend, tripSpend);
    }

    @Override
    public String getReportsCsv(UserPrincipal currentUser) {
        String roleStr = currentUser.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_Employee");

        boolean isManagerOrAdmin = roleStr.equals("ROLE_Manager") || roleStr.equals("ROLE_Admin");

        List<Expense> expenses;
        if (isManagerOrAdmin) {
            expenses = expenseRepository.findAll();
        } else {
            expenses = expenseRepository.findAll().stream()
                    .filter(e -> e.getTrip().getUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Expense ID,Trip ID,Trip Destination,User,Description,Amount,Category,Date,Created At\n");
        for (Expense e : expenses) {
            csv.append(e.getId()).append(",")
               .append(e.getTrip().getId()).append(",")
               .append("\"").append(e.getTrip().getDestination().replace("\"", "\"\"")).append("\",")
               .append("\"").append(e.getTrip().getUser().getFullName().replace("\"", "\"\"")).append("\",")
               .append("\"").append(e.getDescription().replace("\"", "\"\"")).append("\",")
               .append(e.getAmount()).append(",")
               .append(e.getCategory().name()).append(",")
               .append(e.getDate()).append(",")
               .append(e.getCreatedAt()).append("\n");
        }
        return csv.toString();
    }
}
