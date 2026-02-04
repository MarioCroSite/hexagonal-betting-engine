package com.mario.hexagonalbettingengine.domain.betting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.PENDING;
import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.WON;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.*;
import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class BetTest {

    @Test
    @DisplayName("Should return TRUE when winner ID matches selection")
    void shouldReturnTrueWhenWinnerMatches() {
        // Given
        var bet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);

        // When
        var result = bet.isWinner(REAL_MADRID);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return FALSE when winner ID does not match")
    void shouldReturnFalseWhenWinnerDoesNotMatch() {
        // Given
        var bet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);

        // When
        var result = bet.isWinner(BARCELONA);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return FALSE when actual winner is null (match not played yet)")
    void shouldReturnFalseWhenActualWinnerIsNull() {
        // Given
        var bet = pendingBet().build();

        // When
        var result = bet.isWinner(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should create NEW instance with updated status and preserve other fields")
    void shouldCreateNewInstanceOnStatusChange() {
        // Given
        var originalBet = baseBet()
                .betId("bet-123")
                .build();

        // When
        var wonBet = originalBet.withStatus(WON);

        // Then
        assertThat(wonBet.status()).isEqualTo(WON);
        assertThat(wonBet)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(originalBet);

        assertThat(originalBet.status()).isEqualTo(PENDING);
        assertThat(wonBet).isNotSameAs(originalBet);
    }
}