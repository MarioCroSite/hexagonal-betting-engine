package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.domain.betting.BetSettlement;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper.EventOutcomeMapper;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.createOutcome;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventOutcomeListenerAdapterTest {

    @Mock
    private BetSettlement betSettlement;

    @Mock
    private EventOutcomeMapper mapper;

    @InjectMocks
    private EventOutcomeListenerAdapter listener;

    @Test
    @DisplayName("Should orchestrate mapping and settlement flow correctly")
    void shouldMapAndSettleOutcome() {
        // Given
        var payload = EventOutcomePayload.builder()
                .eventId("match-100")
                .eventName("El Classico")
                .eventWinnerId("REAL_MADRID")
                .build();

        var domainOutcome = createOutcome("match-100", "REAL_MADRID");
        when(mapper.toDomain(payload)).thenReturn(domainOutcome);

        // When
        listener.onEventOutcome(payload);

        // Then
        verify(mapper).toDomain(payload);
        verify(betSettlement).settle(domainOutcome);
    }

    @Test
    @DisplayName("Should handle payload with NULL winner (e.g. Cancelled Match)")
    void shouldHandleNullWinner() {
        // Given
        var payload = EventOutcomePayload.builder()
                .eventId("match-999")
                .eventName("Cancelled")
                .eventWinnerId(null)
                .build();

        var domainOutcome = createOutcome("match-999", "REAL_MADRID")
                .toBuilder().eventWinnerId(null).build();

        when(mapper.toDomain(payload)).thenReturn(domainOutcome);

        // When
        listener.onEventOutcome(payload);

        // Then
        verify(betSettlement).settle(domainOutcome);
    }

    @Test
    @DisplayName("Should propagate exception if settlement fails (Critical for Kafka Retry)")
    void shouldPropagateException() {
        // Given
        var payload = EventOutcomePayload.builder().eventId("match-1").build();
        var domainOutcome = createOutcome("match-1", "A");

        when(mapper.toDomain(payload)).thenReturn(domainOutcome);

        doThrow(new RuntimeException("Settlement failed"))
                .when(betSettlement).settle(domainOutcome);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                listener.onEventOutcome(payload)
        );
    }
}