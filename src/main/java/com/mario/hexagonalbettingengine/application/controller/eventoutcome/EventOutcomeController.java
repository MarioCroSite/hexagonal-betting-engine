package com.mario.hexagonalbettingengine.application.controller.eventoutcome;

import com.mario.hexagonalbettingengine.application.controller.mapper.EventOutcomeDtoMapper;
import com.mario.hexagonalbettingengine.application.controller.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcomeCommandHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.ACCEPTED;

@Slf4j
@RestController
@RequestMapping("/api/event-outcomes")
@RequiredArgsConstructor
public class EventOutcomeController {

    private final EventOutcomeCommandHandler handler;
    private final EventOutcomeDtoMapper mapper;

    @PostMapping
    public ResponseEntity<Void> placeEventOutcome(@Valid @RequestBody EventOutcomeRequestDto request) {
        log.info("Received request: {}", request);
        handler.handle(mapper.toDomain(request));
        return ResponseEntity.status(ACCEPTED).build();
    }

}
