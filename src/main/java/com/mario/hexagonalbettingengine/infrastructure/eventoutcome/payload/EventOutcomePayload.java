package com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload;

import lombok.Builder;

@Builder
public record EventOutcomePayload(
        String eventId,
        String eventName,
        String eventWinnerId
) {

}
