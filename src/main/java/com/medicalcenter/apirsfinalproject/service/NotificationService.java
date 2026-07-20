package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(String userId, String message);
    List<Notification> getNotificationsForUser(String userId);
    List<Notification> getUnreadNotificationsForUser(String userId);
    void markAsRead(Long notificationId);
}
