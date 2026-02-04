package com.mario.hexagonalbettingengine.infrastructure.betting;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetPayload;
import com.mario.hexagonalbettingengine.infrastructure.config.MessagingProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static com.mario.hexagonalbettingengine.fixtures.BetFixtures.wonBet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetStatus.WON;

@ExtendWith(MockitoExtension.class)
class LoggingBetSettlementPublisherTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private MessagingProperties properties;

    @Mock
    private BetMapper betMapper;

    @InjectMocks
    private LoggingBetSettlementPublisher publisher;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingBetSettlementPublisher.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

    @Test
    @DisplayName("Should log payload to the configured topic")
    void shouldLogPayloadToTopic() {
        // Given
        var topic = "rocket-topic-test";
        var bet = wonBet().build();

        var payload = BetPayload.builder()
                .betId(bet.betId())
                .status(WON)
                .betAmount(bet.betAmount())
                .settledAt(Instant.now())
                .build();

        when(betMapper.toPayload(bet)).thenReturn(payload);
        when(properties.rocketmq().topic()).thenReturn(topic);

        // When
        publisher.publish(bet);

        // Then
        assertThat(logAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getFormattedMessage)
                .first()
                .asString()
                .contains("[MOCK ROCKETMQ]")
                .contains(topic)
                .contains(payload.toString());
    }
}