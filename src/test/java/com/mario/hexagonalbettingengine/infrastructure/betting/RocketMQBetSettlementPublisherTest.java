package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetPayload;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;


import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.DEFAULT_BET_ID;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.wonBet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class RocketMQBetSettlementPublisherTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Mock
    private BetMapper betMapper;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private MessagingProperties properties;

    @Mock
    private SendResult sendResult;

    @InjectMocks
    private RocketMQBetSettlementPublisher publisher;

    @Test
    @DisplayName("Should publish bet successfully when Broker returns SEND_OK")
    void shouldPublishBetSuccessfully() {
        // Given
        var topic = "rocket-topic";
        var bet = wonBet().build();

        var payload = BetPayload.builder()
                .betId(bet.betId())
                .betAmount(bet.betAmount())
                .build();

        when(betMapper.toPayload(bet)).thenReturn(payload);
        when(properties.rocketmq().topic()).thenReturn(topic);

        when(rocketMQTemplate.syncSend(topic, payload)).thenReturn(sendResult);
        when(sendResult.getSendStatus()).thenReturn(SendStatus.SEND_OK);

        // When
        publisher.publish(bet);

        // Then
        verify(rocketMQTemplate).syncSend(topic, payload);
    }

    @Test
    @DisplayName("Should throw MessagingException when Broker does not acknowledge (Status != OK)")
    void shouldThrowExceptionWhenStatusNotOk() {
        // Given
        var topic = "rocket-topic";
        var bet = wonBet().build();
        var payload = BetPayload.builder().betId(DEFAULT_BET_ID).build();

        when(betMapper.toPayload(bet)).thenReturn(payload);
        when(properties.rocketmq().topic()).thenReturn(topic);

        when(rocketMQTemplate.syncSend(topic, payload)).thenReturn(sendResult);
        when(sendResult.getSendStatus()).thenReturn(SendStatus.FLUSH_DISK_TIMEOUT);

        // When & Then
        assertThatThrownBy(() -> publisher.publish(bet))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Broker did not acknowledge message");
    }

    @Test
    @DisplayName("Should wrap runtime exceptions into MessagingException")
    void shouldHandleTechnicalFailures() {
        // Given
        var topic = "rocket-topic";
        var bet = wonBet().build();
        var payload = BetPayload.builder().betId(DEFAULT_BET_ID).build();

        when(betMapper.toPayload(bet)).thenReturn(payload);
        when(properties.rocketmq().topic()).thenReturn(topic);

        when(rocketMQTemplate.syncSend(topic, payload))
                .thenThrow(new RuntimeException("RocketMQ is down!"));

        // When & Then
        assertThatThrownBy(() -> publisher.publish(bet))
                .isInstanceOf(MessagingException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }
}