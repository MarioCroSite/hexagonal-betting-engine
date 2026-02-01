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
        var config = properties.kafka().eventOutcomes();
        return TopicBuilder.name(config.topic())
                .partitions(config.partitions())
                .replicas(config.replicas())
                .build();
    }

    @Bean
    public NewTopic eventOutcomesDlqTopic() {
        var config = properties.kafka().eventOutcomes();
        return TopicBuilder.name(config.dlqTopic())
                .partitions(config.dlqPartitions())
                .replicas(config.dlqReplicas())
                .build();
    }
}
