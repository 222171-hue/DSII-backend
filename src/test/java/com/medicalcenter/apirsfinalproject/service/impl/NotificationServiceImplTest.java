package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.entity.Notification;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.repository.NotificationRepository;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationSuccess() {
        User user = new User();
        user.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.createNotification("u1", "Hola");

        assertNotNull(result);
        assertEquals("Hola", result.getMessage());
        assertFalse(result.isRead());
    }

    @Test
    void createNotificationThrowsWhenUserMissing() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> notificationService.createNotification("u1", "Hola"));
    }

    @Test
    void getNotificationsForUserDelegates() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("u1"))
                .thenReturn(List.of(Notification.builder().message("x").build()));

        assertEquals(1, notificationService.getNotificationsForUser("u1").size());
    }

    @Test
    void getUnreadNotificationsForUserDelegates() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc("u1"))
                .thenReturn(List.of(Notification.builder().message("x").build()));

        assertEquals(1, notificationService.getUnreadNotificationsForUser("u1").size());
    }

    @Test
    void markAsReadSuccess() {
        Notification notification = Notification.builder().message("x").build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(1L));
    }
}
