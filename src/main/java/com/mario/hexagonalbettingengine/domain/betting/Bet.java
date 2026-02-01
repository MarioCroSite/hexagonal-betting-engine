package com.mario.hexagonalbettingengine.domain.betting;

import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record Bet(
        String betId,
        String userId,
        String eventId,
        String eventMarketId,
        String eventWinnerId,
        BigDecimal betAmount,
        BetStatus status
) {

    public boolean isWinner(String actualWinnerId) {
        if (actualWinnerId == null || eventWinnerId == null) {
            return false;
        }
        return eventWinnerId.equals(actualWinnerId);
    }

    public Bet withStatus(BetStatus newStatus) {
        return new Bet(
                betId,
                userId,
                eventId,
                eventMarketId,
                eventWinnerId,
                betAmount,
                newStatus
        );
    }
}
