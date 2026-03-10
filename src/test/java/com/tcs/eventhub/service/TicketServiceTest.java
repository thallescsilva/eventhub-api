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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Event event;
    private Participant participant;
    private TicketPurchaseRequest request;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .name("Tech Conference")
                .date(LocalDateTime.now().plusDays(30))
                .location("Convention Center")
                .capacity(100)
                .build();

        participant = Participant.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        request = new TicketPurchaseRequest(1L, "John Doe", "john@example.com");
    }

    @Test
    void shouldPurchaseTicketSuccessfully() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findByEmail("john@example.com")).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            ticket.setPurchasedAt(LocalDateTime.now());
            return ticket;
        });

        TicketResponse response = ticketService.purchase(request);

        assertThat(response).isNotNull();
        assertThat(response.eventName()).isEqualTo("Tech Conference");
        assertThat(event.getCapacity()).isEqualTo(99);
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void shouldThrowWhenEventIsSoldOut() {
        event.setCapacity(0);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sold out");
    }

    @Test
    void shouldThrowWhenParticipantAlreadyHasTicket() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findByEmail("john@example.com")).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already has a ticket");
    }

    @Test
    void shouldDecrementCapacityToZeroWhenLastTicket() {
        event.setCapacity(1);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findByEmail("john@example.com")).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            ticket.setPurchasedAt(LocalDateTime.now());
            return ticket;
        });

        ticketService.purchase(request);

        assertThat(event.getCapacity()).isEqualTo(0);
        verify(eventRepository).save(event);
    }

    @Test
    void shouldReturnTicketsByParticipant() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .event(event)
                .participant(participant)
                .purchasedAt(LocalDateTime.now())
                .build();

        when(participantRepository.existsById(1L)).thenReturn(true);
        when(ticketRepository.findByParticipantId(1L)).thenReturn(List.of(ticket));

        List<TicketResponse> tickets = ticketService.findByParticipant(1L);

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).eventName()).isEqualTo("Tech Conference");
    }

    @Test
    void shouldThrowOnConcurrentPurchase() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findByEmail("john@example.com")).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(false);
        when(eventRepository.save(any(Event.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Event.class.getName(), 1L));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("high demand");
    }

    @Test
    void shouldThrowWhenParticipantNotFoundForHistory() {
        when(participantRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> ticketService.findByParticipant(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Participant not found");
    }
}
