package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;

import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.createEntity;
import static com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus.WON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@EmbeddedKafka(partitions = 1, topics = {"event-outcomes-test"})
class EventOutcomeKafkaConsumerIT extends BaseIT {

    @Autowired
    private KafkaTemplate<String, EventOutcomePayload> kafkaTemplate;

    private static final String TOPIC_NAME = "event-outcomes-test";

    @Test
    @DisplayName("Should consume event outcome from Kafka and settle bets")
    void shouldConsumeEventOutcomeAndSettleBets() {
        // Given
        savePendingBet("bet-1", "match-100", "REAL_MADRID");
        savePendingBet("bet-2", "match-100", "BARCELONA");

        var eventOutcome = new EventOutcomePayload("match-100", "El Classico", "REAL_MADRID");

        // When
        kafkaTemplate.send(TOPIC_NAME, eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var wonBet = betRepository.findById("bet-1").orElseThrow();
                    var lostBet = betRepository.findById("bet-2").orElseThrow();

                    assertThat(wonBet.getStatus()).isEqualTo(WON);
                    assertThat(lostBet.getStatus()).isEqualTo(BetStatus.LOST);
                });
    }

    @Test
    @DisplayName("Should not settle already settled bets (Idempotency)")
    void shouldNotSettleAlreadySettledBets() {
        // Given
        var alreadyWonBet = createEntity("bet-already-won", "match-200", "LIVERPOOL", WON);
        betRepository.save(alreadyWonBet);

        var eventOutcome = new EventOutcomePayload("match-200", "Pool vs Milan", "MILAN");

        // WHEN
        kafkaTemplate.send(TOPIC_NAME, eventOutcome);

        // THEN
        await().during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    var bet = betRepository.findById("bet-already-won").orElseThrow();
                    assertThat(bet.getStatus()).isEqualTo(BetStatus.WON);
                });
    }

    @Test
    @DisplayName("Should handle multiple bets for same event")
    void shouldHandleMultipleBetsForSameEvent() {
        // Given
        savePendingBet("bet-1", "match-300", "LAKERS");
        savePendingBet("bet-2", "match-300", "CELTICS");
        savePendingBet("bet-3", "match-300", "LAKERS");

        var eventOutcome = new EventOutcomePayload("match-300", "NBA Finals", "LAKERS");

        // When
        kafkaTemplate.send(TOPIC_NAME, eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var bets = betRepository.findAll();

                    assertThat(bets).hasSize(3);
                    assertThat(bets)
                            .filteredOn(b -> b.getStatus() == BetStatus.WON)
                            .hasSize(2);

                    assertThat(bets)
                            .filteredOn(b -> b.getStatus() == BetStatus.LOST)
                            .hasSize(1);
                });
    }

    @Test
    @DisplayName("Should consume messages with correct partition key")
    void shouldConsumeMessagesWithPartitionKey() {
        // Given
        savePendingBet("bet-pk-1", "event-key-test", "WINNER_A");

        var eventOutcome = new EventOutcomePayload("event-key-test", "Key Test", "WINNER_A");

        // When
        kafkaTemplate.send(TOPIC_NAME, "event-key-test", eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var bet = betRepository.findById("bet-pk-1").orElseThrow();
                    assertThat(bet.getStatus()).isEqualTo(BetStatus.WON);
                });
    }
}