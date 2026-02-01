package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.domain.betting.BetSettlementPublisher;
import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.rocketmq.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingBetSettlementPublisher implements BetSettlementPublisher {

    private final MessagingProperties properties;
    private final BetMapper betMapper;

    @Override
    public void publish(Bet bet) {
        var payload = betMapper.toPayload(bet);
        var topic = properties.rocketmq().topic();

        log.info("[MOCK ROCKETMQ] Bet settlement published to {} topic: {}", topic, payload);
    }
}