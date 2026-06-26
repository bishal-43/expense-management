package com.travel.expense_management.dto.trip;

import com.travel.expense_management.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        String description,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getBudget(),
                trip.getDescription(),
                trip.getUser().getId(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
