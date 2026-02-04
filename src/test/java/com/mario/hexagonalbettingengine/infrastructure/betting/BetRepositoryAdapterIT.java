package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.*;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.pendingBet;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.wonBet;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({BetRepositoryAdapter.class, BetMapperImpl.class})
@DisplayName("BetRepositoryAdapter Integration Tests")
class BetRepositoryAdapterIT {

    @Autowired
    private BetRepositoryAdapter adapter;

    @Autowired
    private BetJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find all pending bets for given event ID")
    void shouldFindPendingBetsByEventId() {
        // Given
        var pendingBet1 = createPendingEntity("bet-1", "match-100", "REAL_MADRID");
        var pendingBet2 = createPendingEntity("bet-2", "match-100", "BARCELONA");

        var wonBet = createEntity("bet-3", "match-100", "DRAW", BetStatus.WON);

        var differentEventBet = createPendingEntity("bet-4", "match-200", "LIVERPOOL");

        jpaRepository.saveAll(List.of(pendingBet1, pendingBet2, wonBet, differentEventBet));

        // When
        var result = adapter.findPendingBetsByEventId("match-100");

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting(Bet::betId)
                .containsExactlyInAnyOrder("bet-1", "bet-2");
    }

    @Test
    @DisplayName("Should return empty list when no pending bets found for event")
    void shouldReturnEmptyListWhenNoPendingBetsFound() {
        // Given
        var wonBet = createEntity("bet-1", "match-100", "WINNER", BetStatus.WON);
        jpaRepository.save(wonBet);

        // When
        var result = adapter.findPendingBetsByEventId("match-100");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save new bet to database (Domain -> Entity Mapping)")
    void shouldSaveNewBet() {
        // Given
        var domainBet = pendingBet()
                .betId("bet-new")
                .eventId("match-500")
                .eventWinnerId("TEAM_A")
                .betAmount(BigDecimal.TEN)
                .build();

        // When
        adapter.save(domainBet);

        // Then
        var savedEntity = jpaRepository.findById("bet-new").orElseThrow();
        assertThat(savedEntity)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(domainBet);
    }

    @Test
    @DisplayName("Should update existing bet in database")
    void shouldUpdateExistingBet() {
        // Given
        var existingEntity = createPendingEntity("bet-update", "match-600", "TEAM_B");
        jpaRepository.save(existingEntity);

        var updatedDomainBet = wonBet()
                .betId("bet-update")
                .eventId("match-600")
                .eventWinnerId("TEAM_B")
                .build();

        // When
        adapter.save(updatedDomainBet);

        // Then
        var savedEntity = jpaRepository.findById("bet-update").orElseThrow();
        assertThat(savedEntity)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(updatedDomainBet);
    }

    @Test
    @DisplayName("Should correctly map entity fields to domain object")
    void shouldMapEntityToDomain() {
        // Given
        var entity = createPendingEntity("bet-map", "match-800", "WINNER_X");
        entity.setBetAmount(BigDecimal.valueOf(123.45));

        jpaRepository.save(entity);

        // When
        var result = adapter.findPendingBetsByEventId("match-800");

        // Then
        assertThat(result).hasSize(1);
        var actualDomainBet = result.getFirst();

        var expectedDomainBet = Bet.builder()
                .betId("bet-map")
                .userId("user-1")
                .eventMarketId("1x2")
                .eventId("match-800")
                .eventWinnerId("WINNER_X")
                .betAmount(BigDecimal.valueOf(123.45))
                .status(com.mario.hexagonalbettingengine.domain.betting.BetStatus.PENDING)
                .build();

        assertThat(actualDomainBet)
                .usingRecursiveComparison()
                .isEqualTo(expectedDomainBet);
    }
}
