package com.tcs.eventhub.controller;

import com.tcs.eventhub.dto.TicketPurchaseRequest;
import com.tcs.eventhub.dto.TicketResponse;
import com.tcs.eventhub.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody TicketPurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.purchase(request));
    }

    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<TicketResponse>> findByParticipant(@PathVariable Long participantId) {
        return ResponseEntity.ok(ticketService.findByParticipant(participantId));
    }
}
