package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.trip.TripRequest;
import com.travel.expense_management.dto.trip.TripResponse;
import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.exception.BadRequestException;
import com.travel.expense_management.exception.ResourceNotFoundException;
import com.travel.expense_management.repository.TripRepository;
import com.travel.expense_management.repository.UserRepository;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.entity.TripStatus;
import com.travel.expense_management.service.AuthorizationService;
import com.travel.expense_management.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    @Override
    public TripResponse createTrip(TripRequest request, UserPrincipal currentUser) {
        validateTripDates(request);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));

        Trip trip = Trip.builder()
                .destination(request.destination())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .budget(request.budget())
                .description(request.description())
                .user(user)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        return TripResponse.from(savedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips(UserPrincipal currentUser) {
        boolean hasPrivilegedRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin") || auth.getAuthority().equals("ROLE_Manager"));

        List<Trip> trips;
        if (hasPrivilegedRole) {
            trips = tripRepository.findAll();
        } else {
            trips = tripRepository.findByUserId(currentUser.getId());
        }

        return trips.stream()
                .map(TripResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));

        authorizationService.authorizeTripAccess(trip, currentUser);

        return TripResponse.from(trip);
    }

    @Override
    public TripResponse updateTrip(Long id, TripRequest request, UserPrincipal currentUser) {
        validateTripDates(request);

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));

        authorizationService.authorizeTripAccess(trip, currentUser);

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot update a trip that is already " + trip.getStatus());
        }

        // Validate any existing expenses do not conflict with new dates
        boolean hasInvalidExpenses = trip.getExpenses().stream()
                .anyMatch(expense -> expense.getDate().isBefore(request.startDate()) || expense.getDate().isAfter(request.endDate()));
        if (hasInvalidExpenses) {
            throw new BadRequestException("Cannot update trip dates: some existing expenses fall outside the new trip dates.");
        }

        trip.setDestination(request.destination());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setBudget(request.budget());
        trip.setDescription(request.description());

        Trip updatedTrip = tripRepository.save(trip);
        return TripResponse.from(updatedTrip);
    }

    @Override
    public void deleteTrip(Long id, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));

        authorizationService.authorizeTripAccess(trip, currentUser);

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Cannot delete a trip that is already " + trip.getStatus());
        }

        tripRepository.delete(trip);
    }

    @Override
    public TripResponse approveTrip(Long id, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
        authorizationService.authorizeTripApproval(currentUser);
        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Only PENDING trips can be approved. Current status: " + trip.getStatus());
        }
        trip.setStatus(TripStatus.APPROVED);
        Trip updatedTrip = tripRepository.save(trip);
        return TripResponse.from(updatedTrip);
    }

    @Override
    public TripResponse rejectTrip(Long id, UserPrincipal currentUser) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
        authorizationService.authorizeTripApproval(currentUser);
        if (trip.getStatus() != TripStatus.PENDING) {
            throw new BadRequestException("Only PENDING trips can be rejected. Current status: " + trip.getStatus());
        }
        trip.setStatus(TripStatus.REJECTED);
        Trip updatedTrip = tripRepository.save(trip);
        return TripResponse.from(updatedTrip);
    }

    private void validateTripDates(TripRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("Trip end date cannot be before the start date.");
        }
    }
}
