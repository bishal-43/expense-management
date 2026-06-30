package com.travel.expense_management.service;

import com.travel.expense_management.dto.trip.TripRequest;
import com.travel.expense_management.dto.trip.TripResponse;
import com.travel.expense_management.security.UserPrincipal;

import java.util.List;

public interface TripService {

    TripResponse createTrip(TripRequest request, UserPrincipal currentUser);

    List<TripResponse> getAllTrips(UserPrincipal currentUser);

    TripResponse getTripById(Long id, UserPrincipal currentUser);

    TripResponse updateTrip(Long id, TripRequest request, UserPrincipal currentUser);

    void deleteTrip(Long id, UserPrincipal currentUser);

    TripResponse approveTrip(Long id, UserPrincipal currentUser);

    TripResponse rejectTrip(Long id, UserPrincipal currentUser);

    TripResponse reimburseTrip(Long id, UserPrincipal currentUser);
}
