package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

public record EventOutcomePayload(
        String eventId,
        String eventName,
        String eventWinnerId
) {

}
