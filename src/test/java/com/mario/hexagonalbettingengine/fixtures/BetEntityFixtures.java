package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.infrastructure.betting.BetEntity;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;

import java.math.BigDecimal;

public class BetEntityFixtures {

    public static final String DEFAULT_BET_ID = "bet-entity-1";
    public static final String DEFAULT_USER_ID = "user-1";
    public static final String DEFAULT_EVENT_ID = "match-100";
    public static final String DEFAULT_MARKET_ID = "1x2";
    public static final String DEFAULT_WINNER_ID = "REAL_MADRID";
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");

    public static BetEntity.BetEntityBuilder baseEntity() {
        return BetEntity.builder()
                .betId(DEFAULT_BET_ID)
                .userId(DEFAULT_USER_ID)
                .eventId(DEFAULT_EVENT_ID)
                .eventMarketId(DEFAULT_MARKET_ID)
                .eventWinnerId(DEFAULT_WINNER_ID)
                .betAmount(DEFAULT_AMOUNT)
                .status(BetStatus.PENDING);
    }

    public static BetEntity.BetEntityBuilder pendingEntity() {
        return baseEntity().status(BetStatus.PENDING);
    }

    public static BetEntity.BetEntityBuilder wonEntity() {
        return baseEntity().status(BetStatus.WON);
    }

    public static BetEntity.BetEntityBuilder lostEntity() {
        return baseEntity().status(BetStatus.LOST);
    }

    public static BetEntity createEntity(String betId, String eventId, String predictedWinner, BetStatus status) {
        return baseEntity()
                .betId(betId)
                .eventId(eventId)
                .eventWinnerId(predictedWinner)
                .status(status)
                .build();
    }

    public static BetEntity createPendingEntity(String betId, String eventId, String predictedWinner) {
        return createEntity(betId, eventId, predictedWinner, BetStatus.PENDING);
    }
}