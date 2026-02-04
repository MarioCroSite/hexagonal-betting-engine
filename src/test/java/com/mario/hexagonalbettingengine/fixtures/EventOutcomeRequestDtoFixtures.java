package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;

public class EventOutcomeRequestDtoFixtures {

    public static final String DEFAULT_EVENT_ID = "match-100";
    public static final String DEFAULT_EVENT_NAME = "Real Madrid vs Barcelona";
    public static final String DEFAULT_WINNER_ID = "REAL_MADRID";

    public static EventOutcomeRequestDto.EventOutcomeRequestDtoBuilder baseRequest() {
        return EventOutcomeRequestDto.builder()
                .eventId(DEFAULT_EVENT_ID)
                .eventName(DEFAULT_EVENT_NAME)
                .eventWinnerId(DEFAULT_WINNER_ID);
    }

    public static EventOutcomeRequestDto validRequest() {
        return baseRequest().build();
    }

    public static EventOutcomeRequestDto createInvalidRequest(String id, String name, String winner) {
        return EventOutcomeRequestDto.builder()
                .eventId(id)
                .eventName(name)
                .eventWinnerId(winner)
                .build();
    }
}
