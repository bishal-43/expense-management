package com.travel.expense_management.dto.notification;

import com.travel.expense_management.entity.Notification;
import java.time.format.DateTimeFormatter;

public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        String createdAt
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt() != null ? notification.getCreatedAt().format(FORMATTER) : null
        );
    }
}
