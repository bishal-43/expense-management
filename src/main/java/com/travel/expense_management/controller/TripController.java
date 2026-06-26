package com.travel.expense_management.controller;

import com.travel.expense_management.dto.trip.TripRequest;
import com.travel.expense_management.dto.trip.TripResponse;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.TripService;
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
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.createTrip(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<TripResponse> response = tripService.getAllTrips(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.getTripById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TripResponse response = tripService.updateTrip(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        tripService.deleteTrip(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
