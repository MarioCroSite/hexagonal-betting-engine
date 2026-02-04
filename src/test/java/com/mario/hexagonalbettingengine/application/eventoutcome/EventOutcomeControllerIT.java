package com.mario.hexagonalbettingengine.application.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;

import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.DEFAULT_EVENT_ID;
import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.DEFAULT_WINNER_ID;
import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeRequestDtoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EmbeddedKafka(partitions = 1, topics = {"event-outcomes-test"})
class EventOutcomeControllerIT extends BaseIT {

    @Test
    @DisplayName("E2E: Should accept outcome, process Kafka message, and settle bets in DB")
    void shouldProcessEventOutcomeAndSettleBets() throws Exception {
        // Given
        savePendingBet("bet-1", DEFAULT_EVENT_ID, DEFAULT_WINNER_ID);
        savePendingBet("bet-2", DEFAULT_EVENT_ID, "BARCELONA");

        var request = validRequest();

        // When
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Then
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var actualWinner = betRepository.findById("bet-1").orElseThrow();
                    var actualLoser = betRepository.findById("bet-2").orElseThrow();

                    var expectedWinner = BetEntityFixtures.createEntity(
                            "bet-1", DEFAULT_EVENT_ID, DEFAULT_WINNER_ID, BetStatus.WON
                    );

                    assertThat(actualWinner)
                            .usingRecursiveComparison()
                            .isEqualTo(expectedWinner);

                    var expectedLoser = BetEntityFixtures.createEntity(
                            "bet-2", DEFAULT_EVENT_ID, "BARCELONA", BetStatus.LOST
                    );

                    assertThat(actualLoser)
                            .usingRecursiveComparison()
                            .isEqualTo(expectedLoser);
                });
    }

    @Test
    @DisplayName("Should reject invalid request (Validation check)")
    void shouldReturnBadRequestWhenDataIsInvalid() throws Exception {
        // Given
        var request = createInvalidRequest("", "Match", "WINNER");

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle event outcome gracefully when no bets exist")
    void shouldHandleEventOutcomeWhenNoBetsExist() throws Exception {
        // Given
        savePendingBet("bet-ignore-1", "other-match", "winner-1");

        var expectedUnchangedBet = betRepository.findById("bet-ignore-1").orElseThrow();
        long initialCount = betRepository.count();

        var request = baseRequest()
                .eventId("unknown-match-999")
                .build();

        // When
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Then
        assertThat(betRepository.count()).isEqualTo(initialCount);

        var actualBet = betRepository.findById("bet-ignore-1").orElseThrow();
        assertThat(actualBet)
                .usingRecursiveComparison()
                .isEqualTo(expectedUnchangedBet);
    }
}