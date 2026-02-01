package com.mario.hexagonalbettingengine.domain.betting;

import java.math.BigDecimal;

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
        if (actualWinnerId == null || this.eventWinnerId == null) {
            return false;
        }
        return this.eventWinnerId.equals(actualWinnerId);
    }

    public Bet withStatus(BetStatus newStatus) {
        return new Bet(
                this.betId,
                this.userId,
                this.eventId,
                this.eventMarketId,
                this.eventWinnerId,
                this.betAmount,
                newStatus
        );
    }
}
