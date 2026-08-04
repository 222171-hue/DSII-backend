package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Notification;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private CustomUserDetails buildDetails() {
        User user = new User();
        user.setId("u1");
        user.setRol(Role.STUDENT);
        return new CustomUserDetails(user);
    }

    @Test
    void getUserNotificationsReturnsList() {
        when(notificationService.getNotificationsForUser("u1")).thenReturn(List.of(Notification.builder().build()));

        ResponseEntity<List<Notification>> response = notificationController.getUserNotifications(buildDetails());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getUnreadNotificationsReturnsList() {
        when(notificationService.getUnreadNotificationsForUser("u1")).thenReturn(List.of(Notification.builder().build()));

        ResponseEntity<List<Notification>> response = notificationController.getUnreadNotifications(buildDetails());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void markAsReadReturnsNoContent() {
        ResponseEntity<Void> response = notificationController.markAsRead(5L);

        assertEquals(204, response.getStatusCode().value());
        verify(notificationService).markAsRead(5L);
    }
}
