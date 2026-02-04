package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.domain.betting.BetStatus;

import java.math.BigDecimal;

public class BetFixtures {

    public static final String DEFAULT_BET_ID = "bet-default-123";
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");

    public static Bet.BetBuilder baseBet() {
        return Bet.builder()
                .betId(DEFAULT_BET_ID)
                .userId("user-default-456")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(DEFAULT_AMOUNT)
                .status(BetStatus.PENDING);
    }

    public static Bet.BetBuilder pendingBet() {
        return baseBet().status(BetStatus.PENDING);
    }

    public static Bet.BetBuilder wonBet() {
        return baseBet().status(BetStatus.WON);
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
        return wonBet().build();
    }
}