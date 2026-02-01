package com.mario.hexagonalbettingengine.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        @NotNull
        RocketMqConfig rocketmq,
        @NotNull
        KafkaConfig kafka
) {
    public record RocketMqConfig(
            boolean enabled,
            @NotBlank String topic,
            @NotBlank String producerGroup
    ) {
    }

    public record KafkaConfig(
            @NotNull
            EventOutcomesConfig eventOutcomes
    ) {
        public record EventOutcomesConfig(
                @NotBlank String topic,
                @NotBlank String dlqTopic,
                int partitions,
                int replicas,
                int dlqPartitions,
                int dlqReplicas,
                long retryInterval,
                int retryAttempts,
                int concurrency
        ) {
        }
    }
}
