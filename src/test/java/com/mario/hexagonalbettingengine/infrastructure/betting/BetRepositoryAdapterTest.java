package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.PENDING;
import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.DEFAULT_EVENT_ID;
import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.baseEntity;
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
        var entity = baseEntity().status(BetStatus.PENDING).build();
        var domainBet = baseBet().status(PENDING).build();

        when(jpaRepository.findByEventIdAndStatus(DEFAULT_EVENT_ID, BetStatus.PENDING))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainBet);

        // When
        var result = adapter.findPendingBetsByEventId(DEFAULT_EVENT_ID);

        // Then
        assertThat(result).containsExactly(domainBet);
        verify(jpaRepository).findByEventIdAndStatus(DEFAULT_EVENT_ID, BetStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list if no entities found")
    void shouldReturnEmptyList() {
        // Given
        when(jpaRepository.findByEventIdAndStatus(DEFAULT_EVENT_ID, BetStatus.PENDING))
                .thenReturn(emptyList());

        // When
        var result = adapter.findPendingBetsByEventId(DEFAULT_EVENT_ID);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should save bet by mapping to entity and calling JpaRepository")
    void shouldSaveBet() {
        // Given
        var domainBet = baseBet().build();
        var entity = baseEntity().build();

        when(mapper.toEntity(domainBet)).thenReturn(entity);

        // When
        adapter.save(domainBet);

        // Then
        verify(mapper).toEntity(domainBet);
        verify(jpaRepository).save(entity);
    }
}