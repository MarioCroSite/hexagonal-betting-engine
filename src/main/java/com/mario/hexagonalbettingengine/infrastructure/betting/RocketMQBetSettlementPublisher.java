package com.mario.hexagonalbettingengine.infrastructure.betting;

import static java.lang.String.format;
import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.domain.betting.BetSettlementPublisher;
import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetPayload;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.rocketmq.enabled", havingValue = "true")
public class RocketMQBetSettlementPublisher implements BetSettlementPublisher {

    private final RocketMQTemplate rocketMQTemplate;
    private final BetMapper betMapper;
    private final MessagingProperties properties;

    @Override
    public void publish(Bet bet) {
        var payload = betMapper.toPayload(bet);
        var topic = properties.rocketmq().topic();
        sendToBroker(topic, payload);
    }

    private void sendToBroker(String topic, BetPayload payload) {
        try {
            var result = rocketMQTemplate.syncSend(topic, payload);
            var status = result.getSendStatus();

            if (status == SEND_OK) {
                log.info("[REAL ROCKETMQ] Bet {} successfully published to topic: {}", payload.betId(), topic);
            } else {
                throw new MessagingException(format("Broker did not acknowledge message. Status: %s", status));
            }
        } catch (Exception e) {
            log.error("[REAL ROCKETMQ] Failed to publish bet {}. Topic: {}", payload.betId(), topic, e);
            throw new MessagingException("RocketMQ error", e);
        }
    }
}