package com.mario.hexagonalbettingengine.application.eventoutcome;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.hexagonalbettingengine.application.eventoutcome.mapper.EventOutcomeDtoMapper;
import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcomeCommandHandler;
import com.mario.hexagonalbettingengine.fixtures.EventOutcomeFixtures;
import com.mario.hexagonalbettingengine.fixtures.EventOutcomeRequestDtoFixtures;
import com.mario.hexagonalbettingengine.infrastructure.config.JacksonConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventOutcomeController.class)
@Import(JacksonConfig.class)
class EventOutcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventOutcomeCommandHandler handler;

    @MockitoBean
    private EventOutcomeDtoMapper mapper;

    @Test
    @DisplayName("Should accept valid request and delegate to handler")
    void shouldAcceptValidEventOutcome() throws Exception {
        // Given
        var request = EventOutcomeRequestDtoFixtures.validRequest();
        var expectedDomainOutcome = EventOutcomeFixtures.realMadridWin().build();

        when(mapper.toDomain(request)).thenReturn(expectedDomainOutcome);

        // When
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Then
        var outcomeCaptor = ArgumentCaptor.forClass(EventOutcome.class);
        verify(handler).handle(outcomeCaptor.capture());

        assertThat(outcomeCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedDomainOutcome);
    }

    @ParameterizedTest(name = "Should reject invalid request: {0}")
    @MethodSource("invalidRequestsProvider")
    void shouldRejectInvalidRequests(String testName, String id, String name, String winner) throws Exception {
        // Given
        var invalidRequest = EventOutcomeRequestDtoFixtures
                .createInvalidRequest(id, name, winner);

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when JSON is malformed")
    void shouldRejectMalformedJson() throws Exception {
        // Given
        String brokenJson = "{\"eventId\": \"123\", \"eventName\": \"Match\"";

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when Handler fails unexpectedly")
    void shouldHandleUnexpectedServiceError() throws Exception {
        // Given
        var request = EventOutcomeRequestDtoFixtures.validRequest();
        var domainOutcome = EventOutcomeFixtures.realMadridWin().build();

        when(mapper.toDomain(request)).thenReturn(domainOutcome);
        doThrow(new RuntimeException("Database down"))
                .when(handler).handle(domainOutcome);

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }

    @Test
    @DisplayName("Should ignore unknown properties in JSON request")
    void shouldIgnoreUnknownProperties() throws Exception {
        // Given
        var eventId = "match-100";
        var eventName = "Real vs Barca";
        var winnerId = "REAL_MADRID";

        var jsonWithExtraField = """
            {
                "eventId": "%s",
                "eventName": "%s",
                "eventWinnerId": "%s",
                "newField": "newValue"
            }
            """.formatted(eventId, eventName, winnerId);

        var expectedDto = EventOutcomeRequestDto.builder()
                .eventId(eventId)
                .eventName(eventName)
                .eventWinnerId(winnerId)
                .build();

        var domainOutcome = EventOutcomeFixtures.realMadridWin().build();
        when(mapper.toDomain(expectedDto)).thenReturn(domainOutcome);

        // When & Then
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithExtraField))
                .andExpect(status().isAccepted());
    }


    static Stream<Arguments> invalidRequestsProvider() {
        return Stream.of(
                Arguments.of("Missing ID", "", "Name", "Winner"),
                Arguments.of("Missing Name", "ID", "", "Winner"),
                Arguments.of("Missing Winner", "ID", "Name", null)
        );
    }
}