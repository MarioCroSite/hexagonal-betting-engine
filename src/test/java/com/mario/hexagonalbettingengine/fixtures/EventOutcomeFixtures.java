package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;

public class EventOutcomeFixtures {

    public static final String DEFAULT_EVENT_ID = "match-100";
    public static final String DEFAULT_EVENT_NAME = "Real Madrid vs Barcelona";
    public static final String REAL_MADRID = "REAL_MADRID";
    public static final String BARCELONA = "BARCELONA";
    public static final String DRAW = "DRAW";

    public static EventOutcome.EventOutcomeBuilder baseOutcome() {
        return EventOutcome.builder()
                .eventId(DEFAULT_EVENT_ID)
                .eventName(DEFAULT_EVENT_NAME)
                .eventWinnerId(REAL_MADRID);
    }

    public static EventOutcome.EventOutcomeBuilder realMadridWin() {
        return baseOutcome().eventWinnerId(REAL_MADRID);
    }

    public static EventOutcome.EventOutcomeBuilder barcelonaWin() {
        return baseOutcome().eventWinnerId(BARCELONA);
    }

    public static EventOutcome.EventOutcomeBuilder drawOutcome() {
        return baseOutcome().eventWinnerId(DRAW);
    }

    public static EventOutcome createOutcome(String eventId, String winnerId) {
        return baseOutcome()
                .eventId(eventId)
                .eventName("Match " + eventId)
                .eventWinnerId(winnerId)
                .build();
    }
}