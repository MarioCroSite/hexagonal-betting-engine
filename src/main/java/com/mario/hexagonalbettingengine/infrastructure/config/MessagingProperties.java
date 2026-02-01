package com.mario.hexagonalbettingengine.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        @NotNull
        KafkaConfig kafka
) {
    public record KafkaConfig(
            @NotBlank String topicName,
            int partitions,
            int replicas
    ) {

    }
}
