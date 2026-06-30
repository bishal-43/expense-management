package com.travel.expense_management.controller;

import com.travel.expense_management.dto.trip.TripRequest;
import com.travel.expense_management.dto.trip.TripResponse;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Endpoints for managing travel trips")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Create a new trip", description = "Creates a new travel trip for the current authenticated user")
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.createTrip(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all trips", description = "Retrieves all trips belonging to or accessible by the current authenticated user")
    public ResponseEntity<List<TripResponse>> getAllTrips(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<TripResponse> response = tripService.getAllTrips(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID", description = "Retrieves a specific trip by its ID")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.getTripById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a trip", description = "Updates details of an existing trip")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.updateTrip(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a trip", description = "Deletes an existing trip by its ID")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        tripService.deleteTrip(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a trip", description = "Approves a trip. Typically restricted to Managers/Admins.")
    public ResponseEntity<TripResponse> approveTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.approveTrip(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a trip", description = "Rejects a trip. Typically restricted to Managers/Admins.")
    public ResponseEntity<TripResponse> rejectTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.rejectTrip(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reimburse")
    @Operation(summary = "Reimburse a trip", description = "Marks a trip as reimbursed. Restricted to Admins/Finance users.")
    public ResponseEntity<TripResponse> reimburseTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.reimburseTrip(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
