package com.travel.expense_management.service;

import com.travel.expense_management.dto.notification.NotificationResponse;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.security.UserPrincipal;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(UserPrincipal currentUser);
    void markAsRead(Long id, UserPrincipal currentUser);
    void createNotification(User user, String message);
}
