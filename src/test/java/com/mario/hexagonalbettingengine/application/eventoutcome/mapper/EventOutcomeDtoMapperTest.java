package com.mario.hexagonalbettingengine.application.eventoutcome.mapper;

import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.fixtures.EventOutcomeRequestDtoFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EventOutcomeDtoMapperTest {

    private final EventOutcomeDtoMapper mapper = Mappers.getMapper(EventOutcomeDtoMapper.class);

    @Test
    @DisplayName("Should map Request DTO to Domain object")
    void shouldMapRequestToDomain() {
        // Given
        var requestDto = EventOutcomeRequestDtoFixtures.validRequest();

        // When
        var domain = mapper.toDomain(requestDto);

        // Then
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(requestDto);
    }

    @Test
    @DisplayName("Should handle NULL winner ID (e.g. Draw or Cancelled match)")
    void shouldHandleNullWinner() {
        // Given
        var requestDto = EventOutcomeRequestDtoFixtures.baseRequest()
                .eventWinnerId(null)
                .build();

        // When
        var domain = mapper.toDomain(requestDto);

        // Then
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(requestDto);
    }
}