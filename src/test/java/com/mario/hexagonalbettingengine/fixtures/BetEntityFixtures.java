package com.mario.hexagonalbettingengine.fixtures;

import com.mario.hexagonalbettingengine.infrastructure.betting.BetEntity;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;

import java.math.BigDecimal;

public class BetEntityFixtures {

    public static BetEntity.BetEntityBuilder baseEntity() {
        return BetEntity.builder()
                .betId("bet-entity-1")
                .userId("user-1")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(new BigDecimal("100.00"))
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

    public static BetEntity createEntity(String betId, BetStatus status) {
        return baseEntity()
                .betId(betId)
                .status(status)
                .build();
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
        return baseEntity()
                .betId(betId)
                .eventId(eventId)
                .eventWinnerId(predictedWinner)
                .status(BetStatus.PENDING)
                .build();
    }
}