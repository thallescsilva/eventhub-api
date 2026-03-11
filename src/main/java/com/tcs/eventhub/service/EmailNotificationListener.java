package com.tcs.eventhub.service;

import com.tcs.eventhub.domain.entity.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationListener {

    @Async
    @EventListener
    public void onTicketPurchased(TicketService.TicketPurchasedEvent event) {
        Ticket ticket = event.ticket();
        log.info("[EMAIL SIMULATION] Sending confirmation email to {} for event '{}' on {}",
                ticket.getParticipant().getEmail(),
                ticket.getEvent().getName(),
                ticket.getEvent().getDate());
    }
}
