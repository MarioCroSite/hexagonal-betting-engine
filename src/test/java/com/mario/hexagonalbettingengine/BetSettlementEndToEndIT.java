package com.mario.hexagonalbettingengine;

import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetPayload;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EmbeddedKafka(partitions = 1, topics = {"event-outcomes-test"})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "app.messaging.rocketmq.enabled=true"
        }
)
@DisplayName("Bet Settlement End-to-End Integration Tests")
class BetSettlementEndToEndIT extends BaseIT {

    private static final String KAFKA_TOPIC = "event-outcomes-test";
    private static final String ROCKETMQ_TOPIC = "bet-settlements";
    private static final Duration ASYNC_VERIFICATION_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private KafkaTemplate<String, EventOutcomePayload> kafkaTemplate;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @BeforeEach
    void setUpMocks() {
        var mockResult = new SendResult();
        mockResult.setSendStatus(SendStatus.SEND_OK);
        when(rocketMQTemplate.syncSend(eq(ROCKETMQ_TOPIC), any(BetPayload.class)))
                .thenReturn(mockResult);
    }

    @Test
    @DisplayName("Should process full settlement flow: Kafka → DB → RocketMQ")
    void shouldProcessCompleteSettlementFlow() {
        // Given
        var betId = "bet-e2e-001";
        var eventId = "match-el-classico";
        var predictedWinner = "BARCELONA";

        savePendingBet(betId, eventId, predictedWinner);

        var eventOutcome = new EventOutcomePayload(
                eventId,
                "El Classico: Barcelona vs Real Madrid",
                predictedWinner
        );

        // When
        kafkaTemplate.send(KAFKA_TOPIC, eventOutcome);

        // Then
        Awaitility.await()
                .atMost(ASYNC_VERIFICATION_TIMEOUT)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var settledBet = betRepository.findById(betId).orElseThrow(
                            () -> new AssertionError("Bet not found: " + betId)
                    );
                    assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
                });

        var payloadCaptor = ArgumentCaptor.forClass(BetPayload.class);

        verify(rocketMQTemplate, timeout(5000))
                .syncSend(eq(ROCKETMQ_TOPIC), payloadCaptor.capture());

        var expectedPayload = BetPayload.builder()
                .betId(betId)
                .userId("user-1")
                .eventId(eventId)
                .eventMarketId("1x2")
                .eventWinnerId(predictedWinner)
                .betAmount(new java.math.BigDecimal("100.00"))
                .status(com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetStatus.WON)
                .build();

        var capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .usingRecursiveComparison()
                .ignoringFields("settledAt")
                .isEqualTo(expectedPayload);
        assertThat(capturedPayload.settledAt()).isNotNull();
    }
}