package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcomePublisher;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper.EventOutcomeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomePublisherAdapter implements EventOutcomePublisher {

    private final MessagingProperties properties;
    private final EventOutcomeMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(EventOutcome eventOutcome) {
        var payload = mapper.toPayload(eventOutcome);
        var key = payload.eventId();
        var config = properties.kafka().eventOutcomes();

        kafkaTemplate.send(config.topic(), key, payload)
                .whenComplete((result, ex) -> handleCompletion(key, result, ex));
    }

    private void handleCompletion(String key, SendResult<String, Object> result, Throwable ex) {
        if (ex != null) {
            log.error("Could not publish outcome for event {}", key, ex);
        } else {
            log.info("Event {} published. Partition: {}, Offset: {}",
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        }
    }
}
