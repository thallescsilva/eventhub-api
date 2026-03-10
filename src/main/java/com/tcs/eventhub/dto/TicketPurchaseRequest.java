package com.tcs.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketPurchaseRequest(
        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotBlank(message = "Participant name is required")
        String participantName,

        @NotBlank(message = "Participant email is required")
        @Email(message = "Invalid email format")
        String participantEmail
) {
}
