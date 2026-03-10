package com.tcs.eventhub.dto;

import com.tcs.eventhub.domain.entity.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String eventName,
        LocalDateTime eventDate,
        String eventLocation,
        String participantName,
        String participantEmail,
        LocalDateTime purchasedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getEvent().getName(),
                ticket.getEvent().getDate(),
                ticket.getEvent().getLocation(),
                ticket.getParticipant().getName(),
                ticket.getParticipant().getEmail(),
                ticket.getPurchasedAt()
        );
    }
}
