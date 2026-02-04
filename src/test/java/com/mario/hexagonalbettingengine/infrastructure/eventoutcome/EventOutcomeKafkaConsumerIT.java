package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;

import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.createEntity;
import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus.LOST;
import static com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus.WON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@EmbeddedKafka(partitions = 1, topics = {"${app.messaging.kafka.event-outcomes.topic}"})
class EventOutcomeKafkaConsumerIT extends BaseIT {

    @Autowired
    private KafkaTemplate<String, EventOutcomePayload> kafkaTemplate;

    @Autowired
    private MessagingProperties messagingProperties;

    @Test
    @DisplayName("Should consume event outcome from Kafka and settle bets (Full State Verification)")
    void shouldConsumeEventOutcomeAndSettleBets() {
        // Given
        savePendingBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID);
        savePendingBet("bet-2", DEFAULT_EVENT_ID, BARCELONA);

        var eventOutcome = new EventOutcomePayload(DEFAULT_EVENT_ID, DEFAULT_EVENT_NAME, REAL_MADRID);

        // When
        var topic = messagingProperties.kafka().eventOutcomes().topic();
        kafkaTemplate.send(topic, eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var actualWinner = betRepository.findById("bet-1").orElseThrow();
                    var actualLoser = betRepository.findById("bet-2").orElseThrow();

                    var expectedWinner = createEntity("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, WON);

                    assertThat(actualWinner)
                            .usingRecursiveComparison()
                            .isEqualTo(expectedWinner);

                    var expectedLoser = createEntity("bet-2", DEFAULT_EVENT_ID, BARCELONA, LOST);

                    assertThat(actualLoser)
                            .usingRecursiveComparison()
                            .isEqualTo(expectedLoser);
                });
    }

    @Test
    @DisplayName("Should not settle already settled bets (Idempotency)")
    void shouldNotSettleAlreadySettledBets() {
        // Given
        var eventId = "match-200";
        var alreadyWonBet = createEntity("bet-already-won", eventId, "LIVERPOOL", WON);
        betRepository.save(alreadyWonBet);

        var eventOutcome = new EventOutcomePayload(eventId, "Liverpool vs Milan", "MILAN");

        // WHEN
        var topic = messagingProperties.kafka().eventOutcomes().topic();
        kafkaTemplate.send(topic, eventOutcome);

        // THEN
        await().during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    var actualBet = betRepository.findById("bet-already-won").orElseThrow();

                    assertThat(actualBet)
                            .usingRecursiveComparison()
                            .isEqualTo(alreadyWonBet);
                });
    }

    @Test
    @DisplayName("Should handle multiple bets for same event")
    void shouldHandleMultipleBetsForSameEvent() {
        // Given
        var eventId = "match-300";
        var winner = "LAKERS";
        var loser = "CELTICS";

        savePendingBet("bet-1", eventId, winner);
        savePendingBet("bet-2", eventId, loser);
        savePendingBet("bet-3", eventId, winner);

        var eventOutcome = new EventOutcomePayload(eventId, "NBA Finals", winner);

        // When
        var topic = messagingProperties.kafka().eventOutcomes().topic();
        kafkaTemplate.send(topic, eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var bet1 = betRepository.findById("bet-1").orElseThrow();
                    var bet2 = betRepository.findById("bet-2").orElseThrow();
                    var bet3 = betRepository.findById("bet-3").orElseThrow();

                    var expectedWon1 = createEntity("bet-1", eventId, winner, WON);
                    var expectedLost = createEntity("bet-2", eventId, loser, LOST);
                    var expectedWon2 = createEntity("bet-3", eventId, winner, WON);

                    assertThat(bet1).usingRecursiveComparison().isEqualTo(expectedWon1);
                    assertThat(bet2).usingRecursiveComparison().isEqualTo(expectedLost);
                    assertThat(bet3).usingRecursiveComparison().isEqualTo(expectedWon2);
                });
    }

    @Test
    @DisplayName("Should consume messages with correct partition key")
    void shouldConsumeMessagesWithPartitionKey() {
        // Given
        var eventId = "event-key-test";
        var winner = "WINNER_A";
        savePendingBet("bet-pk-1", eventId, winner);

        var eventOutcome = new EventOutcomePayload(eventId, "Key Test", winner);

        // When
        var topic = messagingProperties.kafka().eventOutcomes().topic();
        kafkaTemplate.send(topic, eventId, eventOutcome);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var actualBet = betRepository.findById("bet-pk-1").orElseThrow();
                    var expectedBet = createEntity("bet-pk-1", eventId, winner, WON);

                    assertThat(actualBet)
                            .isEqualTo(expectedBet);
                });
    }
}