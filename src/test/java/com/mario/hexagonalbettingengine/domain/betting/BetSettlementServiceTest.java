package com.mario.hexagonalbettingengine.domain.betting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.*;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.*;
import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetSettlementServiceTest {

    @Mock
    private BetRepository repository;

    @Mock
    private BetSettlementPublisher publisher;

    @InjectMocks
    private BetSettlementService service;

    @Captor
    private ArgumentCaptor<Bet> betCaptor;

    @Test
    @DisplayName("Should settle multiple bets (mixed WON/LOST) and publish events")
    void shouldSettleMultipleBetsCorrectly() {
        // Given
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        var bet1 = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);
        var bet2 = createBet("bet-2", DEFAULT_EVENT_ID, "BARCELONA", PENDING);
        var bet3 = createBet("bet-3", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);

        given(repository.findPendingBetsByEventId(DEFAULT_EVENT_ID))
                .willReturn(List.of(bet1, bet2, bet3));

        // When
        service.settle(outcome);

        // Then
        verify(repository, times(3)).save(betCaptor.capture());
        var savedBets = betCaptor.getAllValues();

        assertThat(savedBets)
                .extracting(Bet::betId, Bet::status)
                .containsExactlyInAnyOrder(
                        tuple("bet-1", WON),
                        tuple("bet-2", LOST),
                        tuple("bet-3", WON)
                );

        var inOrder = inOrder(repository, publisher);
        savedBets.forEach(bet -> {
            inOrder.verify(repository).save(bet);
            inOrder.verify(publisher).publish(bet);
        });
    }

    @Test
    @DisplayName("Should preserve bet details (amount, user) when settling")
    void shouldPreserveBetDetails() {
        // Given
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        var originalBet = baseBet()
                .betId("bet-99")
                .userId("special-user")
                .betAmount(new BigDecimal("100.50"))
                .build();

        given(repository.findPendingBetsByEventId(DEFAULT_EVENT_ID))
                .willReturn(List.of(originalBet));

        // When
        service.settle(outcome);

        // Them
        verify(repository).save(betCaptor.capture());
        var savedBet = betCaptor.getValue();

        assertThat(savedBet)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(originalBet);
        assertThat(savedBet.status()).isEqualTo(WON);
    }

    @Test
    @DisplayName("Should do nothing when no pending bets are found")
    void shouldDoNothingWhenNoBetsFound() {
        // Given
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        given(repository.findPendingBetsByEventId(DEFAULT_EVENT_ID))
                .willReturn(emptyList());

        // When
        service.settle(outcome);

        // Then
        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Should propagate exception if repository fails")
    void shouldPropagateExceptionWhenRepositoryFails() {
        // Given
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);
        var bet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);

        given(repository.findPendingBetsByEventId(DEFAULT_EVENT_ID)).willReturn(List.of(bet));

        var expectedSettledBet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, WON);
        doThrow(new RuntimeException("DB Error")).when(repository).save(expectedSettledBet);

        // When & Then
        assertThrows(RuntimeException.class, () -> service.settle(outcome));
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Should propagate exception if publisher fails")
    void shouldPropagateExceptionWhenPublisherFails() {
        // Given
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);
        var bet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, PENDING);

        given(repository.findPendingBetsByEventId(DEFAULT_EVENT_ID)).willReturn(List.of(bet));

        var expectedSettledBet = createBet("bet-1", DEFAULT_EVENT_ID, REAL_MADRID, WON);
        doThrow(new RuntimeException("Kafka Error")).when(publisher).publish(expectedSettledBet);

        // When & Then
        assertThrows(RuntimeException.class, () -> service.settle(outcome));
        verify(repository).save(expectedSettledBet);
    }
}