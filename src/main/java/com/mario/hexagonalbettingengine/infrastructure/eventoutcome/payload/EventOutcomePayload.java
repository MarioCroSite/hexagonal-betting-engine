package com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload;

public record EventOutcomePayload(
        String eventId,
        String eventName,
        String eventWinnerId
) {

}
