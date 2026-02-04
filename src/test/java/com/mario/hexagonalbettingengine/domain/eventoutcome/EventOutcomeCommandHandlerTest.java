package com.mario.hexagonalbettingengine.domain.eventoutcome;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventOutcomeCommandHandlerTest {

    @Mock
    private EventOutcomePublisher publisher;

    @InjectMocks
    private EventOutcomeCommandHandler handler;

    @Test
    @DisplayName("Should strictly delegate event outcome to publisher")
    void shouldPublishEventOutcomeWhenHandled() {
        // Given
        var eventOutcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        // When
        handler.handle(eventOutcome);

        // Then
        verify(publisher).publish(eventOutcome);
    }

    @Test
    @DisplayName("Should handle event outcome even if winner is null (Draw or Cancelled)")
    void shouldHandleEventOutcomeWithNullWinner() {
        // Given
        var eventOutcome = createOutcome("match-200", REAL_MADRID)
                .toBuilder()
                .eventWinnerId(null)
                .build();

        // When
        handler.handle(eventOutcome);

        // Then
        verify(publisher).publish(eventOutcome);
    }

    @Test
    @DisplayName("Should propagate exception when publisher fails (e.g. Messaging System Down)")
    void shouldPropagateExceptionWhenPublisherFails() {
        // Given
        var eventOutcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        doThrow(new RuntimeException("Messaging system unavailable"))
                .when(publisher).publish(eventOutcome);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                handler.handle(eventOutcome)
        );
    }
}