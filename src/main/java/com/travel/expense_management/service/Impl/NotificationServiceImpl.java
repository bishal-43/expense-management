package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.notification.NotificationResponse;
import com.travel.expense_management.entity.Notification;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.exception.ResourceNotFoundException;
import com.travel.expense_management.repository.NotificationRepository;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UserPrincipal currentUser) {
        return notificationRepository.findByUserIdOrderByIdDesc(currentUser.getId()).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}
