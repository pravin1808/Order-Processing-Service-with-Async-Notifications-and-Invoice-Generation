package com.pravinmali.orderprocessing.dto.response;

import com.pravinmali.orderprocessing.entity.Role;
import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        UUID customerId,
        String name,
        String email,
        Role role
) {
    public static AuthResponse of(String token, UUID customerId, String name, String email, Role role) {
        return new AuthResponse(token, "Bearer", customerId, name, email, role);
    }
}
