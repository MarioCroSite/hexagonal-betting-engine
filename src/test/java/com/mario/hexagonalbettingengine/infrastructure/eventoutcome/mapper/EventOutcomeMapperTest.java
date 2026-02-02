package com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.createOutcome;
import static org.assertj.core.api.Assertions.assertThat;

class EventOutcomeMapperTest {

    private final EventOutcomeMapper mapper = Mappers.getMapper(EventOutcomeMapper.class);

    @Test
    @DisplayName("Should map Domain to Payload")
    void shouldMapDomainToPayload() {
        // Given
        var domain = createOutcome("match-100", "REAL_MADRID");

        // When
        var payload = mapper.toPayload(domain);

        // Then
        assertThat(payload)
                .usingRecursiveComparison()
                .isEqualTo(domain);
    }

    @Test
    @DisplayName("Should map Payload to Domain")
    void shouldMapPayloadToDomain() {
        // Given
        var payload = EventOutcomePayload.builder()
                .eventId("match-100")
                .eventName("El Classico")
                .eventWinnerId("REAL_MADRID")
                .build();

        // When
        var domain = mapper.toDomain(payload);

        // Then
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(payload);
    }

    @Test
    @DisplayName("Should handle NULL values correctly (e.g. Draw or Cancelled match)")
    void shouldHandleNullValues() {
        // Given
        var domain = EventOutcome.builder()
                .eventId("match-200")
                .eventName("Cancelled Match")
                .eventWinnerId(null)
                .build();

        // When
        var payload = mapper.toPayload(domain);

        // Then
        assertThat(payload.eventId()).isEqualTo("match-200");
        assertThat(payload.eventWinnerId()).isNull();
    }
}