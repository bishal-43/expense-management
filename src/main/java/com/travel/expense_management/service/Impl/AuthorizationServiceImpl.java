package com.travel.expense_management.service.Impl;

import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.AuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public void authorizeTripAccess(Trip trip, UserPrincipal currentUser) {
        if (currentUser.getId().equals(trip.getUser().getId())) {
            return;
        }
        boolean hasPrivilegedRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin") || auth.getAuthority().equals("ROLE_Manager"));
        if (!hasPrivilegedRole) {
            throw new AccessDeniedException("Access denied: You do not have permission to access this trip.");
        }
    }

    @Override
    public void authorizeUserAccess(Long userId, UserPrincipal currentUser) {
        if (currentUser.getId().equals(userId)) {
            return;
        }
        boolean hasPrivilegedRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin") || auth.getAuthority().equals("ROLE_Manager"));
        if (!hasPrivilegedRole) {
            throw new AccessDeniedException("Access denied: You do not have permission to access resources for user ID " + userId);
        }
    }

    @Override
    public void authorizeTripApproval(UserPrincipal currentUser) {
        boolean hasPrivilegedRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin") || auth.getAuthority().equals("ROLE_Manager"));
        if (!hasPrivilegedRole) {
            throw new AccessDeniedException("Access denied: Only Managers and Admins can approve or reject trips.");
        }
    }
}
