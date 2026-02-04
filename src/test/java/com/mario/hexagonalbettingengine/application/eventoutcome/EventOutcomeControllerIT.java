package com.mario.hexagonalbettingengine.application.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;

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
        savePendingBet("bet-1", "match-100", "REAL_MADRID");
        savePendingBet("bet-2", "match-100", "BARCELONA");

        var request = EventOutcomeRequestDto.builder()
                .eventId("match-100")
                .eventName("El Classico")
                .eventWinnerId("REAL_MADRID")
                .build();

        // When
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Then
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var winner = betRepository.findById("bet-1").orElseThrow();
                    var loser = betRepository.findById("bet-2").orElseThrow();

                    assertThat(winner.getStatus())
                            .as("Winning bet should be updated to WON")
                            .isEqualTo(BetStatus.WON);

                    assertThat(loser.getStatus())
                            .as("Losing bet should be updated to LOST")
                            .isEqualTo(BetStatus.LOST);
                });
    }

    @Test
    @DisplayName("Should reject invalid request (Validation check)")
    void shouldReturnBadRequestWhenDataIsInvalid() throws Exception {
        // Given
        var request = EventOutcomeRequestDto.builder()
                .eventId("")
                .eventName("Match")
                .eventWinnerId("WINNER")
                .build();

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
        long initialCount = betRepository.count();

        var request = EventOutcomeRequestDto.builder()
                .eventId("unknown-match-999")
                .eventName("Unknown Match")
                .eventWinnerId("SOME_TEAM")
                .build();

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        assertThat(betRepository.count()).isEqualTo(initialCount);
        var existingBet = betRepository.findById("bet-ignore-1").orElseThrow();
        assertThat(existingBet.getStatus()).isEqualTo(BetStatus.PENDING);
    }
}