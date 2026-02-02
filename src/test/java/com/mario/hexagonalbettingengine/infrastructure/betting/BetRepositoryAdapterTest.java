package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetRepositoryAdapterTest {

    @Mock
    private BetJpaRepository jpaRepository;

    @Mock
    private BetMapper mapper;

    @InjectMocks
    private BetRepositoryAdapter adapter;

    @Test
    @DisplayName("Should find pending bets by calling JpaRepository and mapping results")
    void shouldFindPendingBetsByEventId() {
        // Given
        var eventId = "match-100";
        var entity = BetEntity.builder()
                .betId("bet-1")
                .userId("u-1")
                .eventId(eventId)
                .eventMarketId("1x2")
                .eventWinnerId("REAL")
                .betAmount(BigDecimal.TEN)
                .status(BetStatus.PENDING)
                .build();

        var domainBet = pendingBet().betId("bet-1").eventId(eventId).build();

        when(jpaRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainBet);

        // When
        var result = adapter.findPendingBetsByEventId(eventId);

        // Then
        assertThat(result).containsExactly(domainBet);
        verify(jpaRepository).findByEventIdAndStatus(eventId, BetStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list if no entities found")
    void shouldReturnEmptyList() {
        // Given
        var eventId = "match-999";

        when(jpaRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING))
                .thenReturn(emptyList());

        // When
        var result = adapter.findPendingBetsByEventId(eventId);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should save bet by mapping to entity and calling JpaRepository")
    void shouldSaveBet() {
        // Given
        var betId = "bet-1";

        var domainBet = pendingBet().betId(betId).build();

        var entity = new BetEntity();
        entity.setBetId(betId);

        when(mapper.toEntity(domainBet)).thenReturn(entity);

        // When
        adapter.save(domainBet);

        // Then
        verify(mapper).toEntity(domainBet);
        verify(jpaRepository).save(entity);
    }
}