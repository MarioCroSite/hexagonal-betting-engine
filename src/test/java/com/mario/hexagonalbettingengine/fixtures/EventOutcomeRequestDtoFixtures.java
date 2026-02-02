package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;

public class EventOutcomeRequestDtoFixtures {

    public static EventOutcomeRequestDto.EventOutcomeRequestDtoBuilder baseRequest() {
        return EventOutcomeRequestDto.builder()
                .eventId("match-100")
                .eventName("Real Madrid vs Barcelona")
                .eventWinnerId("REAL_MADRID");
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
