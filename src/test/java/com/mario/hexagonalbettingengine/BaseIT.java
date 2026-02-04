package com.mario.hexagonalbettingengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.hexagonalbettingengine.fixtures.BetEntityFixtures;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected BetJpaRepository betRepository;

    @BeforeEach
    void cleanUp() {
        betRepository.deleteAll();
    }

    protected void savePendingBet(String betId, String eventId, String winnerId) {
        var entity = BetEntityFixtures.createPendingEntity(betId, eventId, winnerId);
        betRepository.save(entity);
    }
}
