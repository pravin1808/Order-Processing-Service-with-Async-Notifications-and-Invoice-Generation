package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.entity.Notification;
import com.pravinmali.orderprocessing.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser(username = "pravin@example.com", roles = "CUSTOMER")
    @DisplayName("GET /api/v1/notifications: CUSTOMER receives their notifications")
    void getNotifications_asCustomer_returns200() throws Exception {
        Notification notification = Notification.builder()
                .recipientEmail("pravin@example.com")
                .title("Test Order Placed")
                .message("Your order has been placed")
                .orderId(UUID.randomUUID())
                .eventType("ORDER_CREATED")
                .read(false)
                .build();
        notificationRepository.save(notification);

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].title").value("Test Order Placed"));
    }

    @Test
    @WithMockUser(username = "pravin@example.com", roles = "CUSTOMER")
    @DisplayName("PATCH /api/v1/notifications/{id}/read: marks notification as read")
    void markAsRead_asCustomer_returnsSuccess() throws Exception {
        Notification notification = Notification.builder()
                .recipientEmail("pravin@example.com")
                .title("Unread Alert")
                .message("Please read this")
                .orderId(UUID.randomUUID())
                .eventType("ORDER_CONFIRMED")
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);

        mockMvc.perform(patch("/api/v1/notifications/" + saved.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "pravin@example.com", roles = "CUSTOMER")
    @DisplayName("PATCH /api/v1/notifications/read-all: marks all as read")
    void markAllAsRead_asCustomer_returnsSuccess() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
