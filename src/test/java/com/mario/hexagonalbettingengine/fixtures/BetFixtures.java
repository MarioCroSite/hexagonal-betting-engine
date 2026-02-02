package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.domain.betting.BetStatus;

import java.math.BigDecimal;

public class BetFixtures {

    public static Bet.BetBuilder baseBet() {
        return Bet.builder()
                .betId("bet-default-123")
                .userId("user-default-456")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(new BigDecimal("100.00"))
                .status(BetStatus.PENDING);
    }

    public static Bet.BetBuilder pendingBet() {
        return baseBet().status(BetStatus.PENDING);
    }

    public static Bet.BetBuilder wonBet() {
        return baseBet().status(BetStatus.WON);
    }

    public static Bet.BetBuilder lostBet() {
        return baseBet().status(BetStatus.LOST);
    }

    public static Bet createBet(String betId, String eventId, String winnerId, BetStatus status) {
        return baseBet()
                .betId(betId)
                .eventId(eventId)
                .eventWinnerId(winnerId)
                .status(status)
                .build();
    }

    public static Bet createWonBet() {
        return Bet.builder()
                .betId("bet-1")
                .userId("user-1")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(BigDecimal.TEN)
                .status(BetStatus.WON)
                .build();
    }
}