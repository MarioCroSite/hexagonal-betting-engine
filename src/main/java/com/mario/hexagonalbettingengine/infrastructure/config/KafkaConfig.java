package com.mario.hexagonalbettingengine.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Collection;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private static final String DLQ_SUFFIX = "-dlq";

    private final MessagingProperties properties;

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        var config = properties.kafka().eventOutcomes();

        var recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    var dlqTopic = record.topic() + DLQ_SUFFIX;
                    log.error("Message processing failed. Forwarding to DLQ: {}. Reason: {}",
                            dlqTopic, ex.getMessage());

                    return new TopicPartition(dlqTopic, record.partition());
                });

        return new DefaultErrorHandler(recoverer,
                new FixedBackOff(config.retryInterval(), config.retryAttempts()));
    }

    @Bean("eventOutcomeKafkaContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> eventOutcomeKafkaContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            CommonErrorHandler commonErrorHandler) {

        var config = properties.kafka().eventOutcomes();
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(commonErrorHandler);
        factory.setConcurrency(config.concurrency());

        factory.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() {
            @Override
            public void onPartitionsAssigned(@NonNull Collection<TopicPartition> partitions) {
                log.info("Kafka Rebalance: Assigned partitions: {}", partitions);
            }

            @Override
            public void onPartitionsRevoked(@NonNull Collection<TopicPartition> partitions) {
                log.info("Kafka Rebalance: Revoked partitions: {}", partitions);
            }
        });

        return factory;
    }
}
