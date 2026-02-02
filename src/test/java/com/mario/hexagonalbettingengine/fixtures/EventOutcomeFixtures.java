package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;

public class EventOutcomeFixtures {

    public static EventOutcome.EventOutcomeBuilder baseOutcome() {
        return EventOutcome.builder()
                .eventId("match-100")
                .eventName("Real Madrid vs Barcelona")
                .eventWinnerId("REAL_MADRID");
    }

    public static EventOutcome.EventOutcomeBuilder realMadridWin() {
        return baseOutcome().eventWinnerId("REAL_MADRID");
    }

    public static EventOutcome.EventOutcomeBuilder barcelonaWin() {
        return baseOutcome().eventWinnerId("BARCELONA");
    }

    public static EventOutcome.EventOutcomeBuilder drawOutcome() {
        return baseOutcome().eventWinnerId("DRAW");
    }

    public static EventOutcome.EventOutcomeBuilder liverpoolVsMilan() {
        return baseOutcome()
                .eventId("match-200")
                .eventName("Liverpool vs Milan")
                .eventWinnerId("LIVERPOOL"); // Default pobjednik za ovaj meč
    }

    public static EventOutcome createOutcome(String eventId, String winnerId) {
        return baseOutcome()
                .eventId(eventId)
                .eventName("Match " + eventId)
                .eventWinnerId(winnerId)
                .build();
    }
}