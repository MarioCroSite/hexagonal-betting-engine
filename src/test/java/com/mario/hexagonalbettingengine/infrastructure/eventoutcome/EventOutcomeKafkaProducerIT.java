package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"event-outcomes-test"})
class EventOutcomeKafkaProducerIT extends BaseIT {

    @Autowired
    private EventOutcomePublisherAdapter publisher;

    @Autowired
    private BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records;

    private static final String TOPIC_NAME = "event-outcomes-test";

    @TestConfiguration
    static class KafkaTestListenerConfig {

        private final BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records = new LinkedBlockingQueue<>();

        @Bean
        public BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records() {
            return records;
        }

        @KafkaListener(
                topics = "event-outcomes-test",
                groupId = "producer-it-unique-group",
                properties = {
                        "auto.offset.reset=earliest"
                }
        )
        public void listen(ConsumerRecord<String, EventOutcomePayload> record) {
            records.add(record);
        }
    }

    @BeforeEach
    void setUp() {
        records.clear();
    }

    @Test
    @DisplayName("Should publish event outcome to Kafka topic")
    void shouldPublishEventOutcomeToKafka() throws InterruptedException {
        // Given
        var eventOutcome = EventOutcome.builder()
                .eventId("match-100")
                .eventName("Real Madrid vs Barcelona")
                .eventWinnerId("REAL_MADRID")
                .build();

        // When
        publisher.publish(eventOutcome);

        // Then
        var record = records.poll(5, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.topic()).isEqualTo(TOPIC_NAME);
        assertThat(record.key()).isEqualTo("match-100");
        assertThat(record.value().eventId()).isEqualTo("match-100");
    }

    @Test
    @DisplayName("Should use event ID as message key")
    void shouldUseEventIdAsMessageKey() throws InterruptedException {
        // Given
        var eventOutcome = EventOutcome.builder()
                .eventId("pk-test-123")
                .eventName("Key Test")
                .eventWinnerId("A")
                .build();

        // When
        publisher.publish(eventOutcome);

        // Then
        var record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("pk-test-123");
    }

    @Test
    @DisplayName("Should publish multiple events in order")
    void shouldPublishMultipleEventsInOrder() throws InterruptedException {
        // Given
        publisher.publish(EventOutcome.builder().eventId("1").eventName("A").eventWinnerId("A").build());
        publisher.publish(EventOutcome.builder().eventId("2").eventName("B").eventWinnerId("B").build());

        // Then
        var r1 = records.poll(5, TimeUnit.SECONDS);
        var r2 = records.poll(5, TimeUnit.SECONDS);

        assertThat(r1).isNotNull();
        assertThat(r2).isNotNull();

        assertThat(r1.value().eventId()).isEqualTo("1");
        assertThat(r2.value().eventId()).isEqualTo("2");
    }
}