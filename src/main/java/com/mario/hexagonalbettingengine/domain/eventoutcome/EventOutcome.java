package com.mario.hexagonalbettingengine.domain.eventoutcome;

import lombok.Builder;

@Builder(toBuilder = true)
public record EventOutcome(
        String eventId,
        String eventName,
        String eventWinnerId
) {

}
