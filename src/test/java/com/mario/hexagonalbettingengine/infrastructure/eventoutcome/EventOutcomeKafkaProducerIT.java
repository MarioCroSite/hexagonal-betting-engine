package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.BaseIT;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
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

import static com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"${app.messaging.kafka.event-outcomes.topic}"})
class EventOutcomeKafkaProducerIT extends BaseIT {

    @Autowired
    private EventOutcomePublisherAdapter publisher;

    @Autowired
    private MessagingProperties messagingProperties;

    @Autowired
    private BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records;

    @TestConfiguration
    static class KafkaTestListenerConfig {

        private final BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records = new LinkedBlockingQueue<>();

        @Bean
        public BlockingQueue<ConsumerRecord<String, EventOutcomePayload>> records() {
            return records;
        }

        @KafkaListener(
                topics = "${app.messaging.kafka.event-outcomes.topic}",
                groupId = "producer-it-unique-group",
                properties = {"auto.offset.reset=earliest"}
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
        var eventOutcome = createOutcome(DEFAULT_EVENT_ID, REAL_MADRID);

        // When
        publisher.publish(eventOutcome);

        // Then
        var record = records.poll(5, TimeUnit.SECONDS);

        assertThat(record).isNotNull();

        assertThat(record.topic())
                .isEqualTo(messagingProperties.kafka().eventOutcomes().topic());

        assertThat(record.key()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(record.value().eventId()).isEqualTo(DEFAULT_EVENT_ID);
    }

    @Test
    @DisplayName("Should use event ID as message key")
    void shouldUseEventIdAsMessageKey() throws InterruptedException {
        // Given
        var pkEventId = "pk-test-123";
        var eventOutcome = createOutcome(pkEventId, "A");

        // When
        publisher.publish(eventOutcome);

        // Then
        var record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(pkEventId);
    }

    @Test
    @DisplayName("Should publish multiple events in order")
    void shouldPublishMultipleEventsInOrder() throws InterruptedException {
        // Given
        publisher.publish(createOutcome("1", "A"));
        publisher.publish(createOutcome("2", "B"));

        // Then
        var r1 = records.poll(5, TimeUnit.SECONDS);
        var r2 = records.poll(5, TimeUnit.SECONDS);

        assertThat(r1).isNotNull();
        assertThat(r2).isNotNull();

        assertThat(r1.value().eventId()).isEqualTo("1");
        assertThat(r2.value().eventId()).isEqualTo("2");
    }
}