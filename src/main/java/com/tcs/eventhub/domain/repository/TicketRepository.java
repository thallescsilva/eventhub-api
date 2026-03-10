package com.tcs.eventhub.domain.repository;

import com.tcs.eventhub.domain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByParticipantId(Long participantId);

    List<Ticket> findByEventId(Long eventId);

    boolean existsByEventIdAndParticipantId(Long eventId, Long participantId);
}
