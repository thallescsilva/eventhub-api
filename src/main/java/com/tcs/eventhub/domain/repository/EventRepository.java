package com.tcs.eventhub.domain.repository;

import com.tcs.eventhub.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
