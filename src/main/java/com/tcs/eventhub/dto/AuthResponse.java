package com.tcs.eventhub.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {
}
