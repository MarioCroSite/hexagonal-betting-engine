package com.mario.hexagonalbettingengine.domain.eventoutcome;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventOutcomeCommandHandler {

    private final EventOutcomePublisher publisher;

    public void handle(EventOutcome eventOutcome) {
        publisher.publish(eventOutcome);
    }
}
