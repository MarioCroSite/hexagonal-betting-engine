package com.mario.hexagonalbettingengine;

import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MessagingProperties.class)
public class HexagonalBettingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(HexagonalBettingEngineApplication.class, args);
    }

}
