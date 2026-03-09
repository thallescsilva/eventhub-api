package com.tcs.eventhub.dto;

import com.tcs.eventhub.domain.entity.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        LocalDateTime date,
        String location,
        Integer capacity
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDate(),
                event.getLocation(),
                event.getCapacity()
        );
    }
}
