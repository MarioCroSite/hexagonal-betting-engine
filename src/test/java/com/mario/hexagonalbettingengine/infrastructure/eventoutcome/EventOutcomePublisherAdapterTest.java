package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper.EventOutcomeMapper;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class EventOutcomePublisherAdapterTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private MessagingProperties properties;

    @Mock
    private EventOutcomeMapper mapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventOutcomePublisherAdapter publisher;

    @Test
    @DisplayName("Should publish event outcome to correct Kafka topic with EventId as key")
    void shouldPublishToKafkaSuccessfully() {
        // Given
        var topicName = "event-outcomes-topic";
        var outcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        var payload = EventOutcomePayload.builder()
                .eventId(DEFAULT_EVENT_ID)
                .eventName(DEFAULT_EVENT_NAME)
                .eventWinnerId(REAL_MADRID)
                .build();

        when(mapper.toPayload(outcome)).thenReturn(payload);
        when(properties.kafka().eventOutcomes().topic()).thenReturn(topicName);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // When
        publisher.publish(outcome);

        // Then
        verify(kafkaTemplate).send(topicName, DEFAULT_EVENT_ID, payload);
    }

    @Test
    @DisplayName("Should handle Kafka failure gracefully (log error without crashing)")
    void shouldHandleKafkaFailure() {
        // Given
        var topic = "event-outcomes-topic";
        var eventId = DEFAULT_EVENT_ID;

        var outcome = createOutcome(eventId, REAL_MADRID);
        var payload = EventOutcomePayload.builder().eventId(eventId).build();

        when(mapper.toPayload(outcome)).thenReturn(payload);
        when(properties.kafka().eventOutcomes().topic()).thenReturn(topic);

        var failedFuture = new CompletableFuture<SendResult<String, Object>>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka is down!"));

        when(kafkaTemplate.send(topic, eventId, payload))
                .thenReturn(failedFuture);

        // When
        publisher.publish(outcome);

        // Then
        verify(kafkaTemplate).send(topic, eventId, payload);
    }
}