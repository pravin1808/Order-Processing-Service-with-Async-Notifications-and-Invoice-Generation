package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.dto.response.NotificationListResponse;
import com.pravinmali.orderprocessing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User async notifications endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications and unread count")
    public ResponseEntity<NotificationListResponse> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        NotificationListResponse response = notificationService.getNotificationsForUser(email, role);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") UUID id,
                                                          Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        boolean updated = notificationService.markAsRead(id, email, role);
        return ResponseEntity.ok(Map.of("success", updated));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all user notifications as read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        notificationService.markAllAsRead(email, role);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
