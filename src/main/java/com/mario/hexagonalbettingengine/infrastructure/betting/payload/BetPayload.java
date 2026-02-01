package com.mario.hexagonalbettingengine.infrastructure.betting.payload;

import java.math.BigDecimal;
import java.time.Instant;

public record BetPayload(
        String betId,
        String userId,
        String eventId,
        String eventMarketId,
        String eventWinnerId,
        BigDecimal betAmount,
        BetStatus status,
        Instant settledAt
) {

}
