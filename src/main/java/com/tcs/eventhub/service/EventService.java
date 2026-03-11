package com.tcs.eventhub.service;

import com.tcs.eventhub.domain.entity.Event;
import com.tcs.eventhub.domain.repository.EventRepository;
import com.tcs.eventhub.dto.EventRequest;
import com.tcs.eventhub.dto.EventResponse;
import com.tcs.eventhub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Cacheable("events")
    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAll().stream()
                .map(EventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @CacheEvict(value = "events", allEntries = true)
    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = Event.builder()
                .name(request.name())
                .date(request.date())
                .location(request.location())
                .capacity(request.capacity())
                .build();
        return EventResponse.from(eventRepository.save(event));
    }

    @CacheEvict(value = "events", allEntries = true)
    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        event.setName(request.name());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setCapacity(request.capacity());

        return EventResponse.from(eventRepository.save(event));
    }

    @CacheEvict(value = "events", allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }
}
