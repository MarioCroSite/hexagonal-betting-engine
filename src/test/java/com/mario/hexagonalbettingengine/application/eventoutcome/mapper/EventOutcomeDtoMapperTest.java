package com.mario.hexagonalbettingengine.application.eventoutcome.mapper;

import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
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
        var requestDto = EventOutcomeRequestDto.builder()
                .eventId("match-100")
                .eventName("Real vs Barca")
                .eventWinnerId("REAL_MADRID")
                .build();

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
        var requestDto = EventOutcomeRequestDto.builder()
                .eventId("match-200")
                .eventName("Friendly Match")
                .eventWinnerId(null)
                .build();

        // When
        var domain = mapper.toDomain(requestDto);

        // Then
        assertThat(domain.eventId()).isEqualTo("match-200");
        assertThat(domain.eventWinnerId()).isNull();
    }
}