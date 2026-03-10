package com.tcs.eventhub.service;

import com.tcs.eventhub.domain.entity.Event;
import com.tcs.eventhub.domain.entity.Participant;
import com.tcs.eventhub.domain.entity.Ticket;
import com.tcs.eventhub.domain.repository.EventRepository;
import com.tcs.eventhub.domain.repository.ParticipantRepository;
import com.tcs.eventhub.domain.repository.TicketRepository;
import com.tcs.eventhub.dto.TicketPurchaseRequest;
import com.tcs.eventhub.dto.TicketResponse;
import com.tcs.eventhub.exception.BusinessException;
import com.tcs.eventhub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse purchase(TicketPurchaseRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.eventId()));

        if (event.getCapacity() <= 0) {
            throw new BusinessException("Event is sold out");
        }

        Participant participant = getOrCreateParticipant(request);

        if (ticketRepository.existsByEventIdAndParticipantId(event.getId(), participant.getId())) {
            throw new BusinessException("Participant already has a ticket for this event");
        }

        event.setCapacity(event.getCapacity() - 1);

        try {
            eventRepository.save(event);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Could not complete purchase due to high demand. Please try again.");
        }

        Ticket ticket = Ticket.builder()
                .event(event)
                .participant(participant)
                .build();

        ticket = ticketRepository.save(ticket);

        log.info("Ticket purchased successfully - Event: {}, Participant: {}", event.getName(), participant.getEmail());

        // simulate email confirmation
        log.info("[EMAIL] Sending confirmation to {} for event '{}'", participant.getEmail(), event.getName());

        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findByParticipant(Long participantId) {
        if (!participantRepository.existsById(participantId)) {
            throw new ResourceNotFoundException("Participant not found with id: " + participantId);
        }
        return ticketRepository.findByParticipantId(participantId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    private Participant getOrCreateParticipant(TicketPurchaseRequest request) {
        return participantRepository.findByEmail(request.participantEmail())
                .orElseGet(() -> participantRepository.save(
                        Participant.builder()
                                .name(request.participantName())
                                .email(request.participantEmail())
                                .build()
                ));
    }
}
