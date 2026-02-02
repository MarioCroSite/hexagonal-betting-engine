package com.mario.hexagonalbettingengine.infrastructure.betting.mapper;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetEntity;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BetMapperTest {

    private final BetMapper mapper = Mappers.getMapper(BetMapper.class);

    @Test
    @DisplayName("Should map Entity to Domain")
    void shouldMapEntityToDomain() {
        // Given
        var entity = BetEntity.builder()
                .betId("bet-1")
                .userId("user-1")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(new BigDecimal("50.00"))
                .status(BetStatus.WON)
                .build();

        // When
        var domain = mapper.toDomain(entity);

        // Then
        assertThat(domain)
                .usingRecursiveComparison()
                .withEnumStringComparison()
                .isEqualTo(entity);
    }

    @Test
    @DisplayName("Should map Domain to Entity")
    void shouldMapDomainToEntity() {
        // Given
        var domain = Bet.builder()
                .betId("bet-1")
                .userId("user-1")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(new BigDecimal("50.00"))
                .status(com.mario.hexagonalbettingengine.domain.betting.BetStatus.PENDING)
                .build();

        // When
        var entity = mapper.toEntity(domain);

        // Then
        assertThat(entity)
                .usingRecursiveComparison()
                .withEnumStringComparison()
                .isEqualTo(domain);
    }

    @Test
    @DisplayName("Should map Domain to Payload and generate 'settledAt' timestamp")
    void shouldMapDomainToPayload() {
        // Given
        var domain = Bet.builder()
                .betId("bet-1")
                .userId("user-1")
                .eventId("match-100")
                .eventMarketId("1x2")
                .eventWinnerId("REAL_MADRID")
                .betAmount(new BigDecimal("75.50"))
                .status(com.mario.hexagonalbettingengine.domain.betting.BetStatus.WON)
                .build();

        // When
        var payload = mapper.toPayload(domain);

        // Then
        assertThat(payload)
                .usingRecursiveComparison()
                .ignoringFields("settledAt")
                .withEnumStringComparison()
                .isEqualTo(domain);
        assertThat(payload.settledAt()).isNotNull();
    }
}