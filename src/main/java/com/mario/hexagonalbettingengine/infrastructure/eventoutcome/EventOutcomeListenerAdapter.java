package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.domain.betting.BetSettlement;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper.EventOutcomeMapper;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeListenerAdapter {

    private final BetSettlement betSettlement;
    private final EventOutcomeMapper mapper;

    @KafkaListener(
            id = "event-outcomes-kafka-consumer",
            topics = "${app.messaging.kafka.event-outcomes.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "eventOutcomeKafkaContainerFactory"
    )
    public void onEventOutcome(@Payload EventOutcomePayload payload) {
        log.info("Received event outcome: eventId={}, eventName={}, winnerId={}",
                payload.eventId(), payload.eventName(), payload.eventWinnerId());

        var eventOutcome = mapper.toDomain(payload);
        betSettlement.settle(eventOutcome);
    }
}
