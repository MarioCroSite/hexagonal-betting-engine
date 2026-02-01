package com.mario.hexagonalbettingengine.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final MessagingProperties properties;

    @Bean
    public NewTopic eventOutcomesTopic() {
        var kafkaProps = properties.kafka();
        return TopicBuilder.name(kafkaProps.topicName())
                .partitions(kafkaProps.partitions())
                .replicas(kafkaProps.replicas())
                .build();
    }
}
