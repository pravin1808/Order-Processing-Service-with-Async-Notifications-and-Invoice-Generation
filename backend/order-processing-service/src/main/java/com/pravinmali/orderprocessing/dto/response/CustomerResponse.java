package com.pravinmali.orderprocessing.dto.response;

import java.util.UUID;

/**
 * Customer details embedded inside order responses.
 */
public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String address
) {}
