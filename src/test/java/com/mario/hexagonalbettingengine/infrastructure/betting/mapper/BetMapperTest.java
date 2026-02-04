package com.mario.hexagonalbettingengine.infrastructure.betting.mapper;

import com.mario.hexagonalbettingengine.infrastructure.betting.BetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures.baseEntity;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.baseBet;
import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.wonBet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BetMapperTest {

    private final BetMapper mapper = Mappers.getMapper(BetMapper.class);

    @Test
    @DisplayName("Should map Entity to Domain")
    void shouldMapEntityToDomain() {
        // Given
        var entity = baseEntity()
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
        var domain = baseBet()
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
        var domain = wonBet().build();
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