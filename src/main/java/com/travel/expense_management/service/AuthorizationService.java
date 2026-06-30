package com.travel.expense_management.service;

import com.travel.expense_management.entity.Trip;
import com.travel.expense_management.security.UserPrincipal;

public interface AuthorizationService {

    void authorizeTripAccess(Trip trip, UserPrincipal currentUser);

    void authorizeTripWrite(Trip trip, UserPrincipal currentUser);

    void authorizeUserAccess(Long userId, UserPrincipal currentUser);

    void authorizeTripApproval(UserPrincipal currentUser);
}
