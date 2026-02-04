package com.mario.hexagonalbettingengine.application.eventoutcome;

import com.mario.hexagonalbettingengine.application.eventoutcome.mapper.EventOutcomeDtoMapper;
import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcomeCommandHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/event-outcomes")
@RequiredArgsConstructor
@Tag(name = "Event Outcomes", description = "Sports event outcome handling and bet settlement")
public class EventOutcomeController {

    private final EventOutcomeCommandHandler handler;
    private final EventOutcomeDtoMapper mapper;

    @PostMapping
    @Operation(
            summary = "Publish event outcome to Kafka",
            description = "Publishes event outcome to Kafka, triggering bet settlement for all pending bets matching the event ID"
    )
    @ApiResponse(responseCode = "202", description = "Event outcome accepted and published successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request - validation failed")
    public ResponseEntity<Void> placeEventOutcome(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "eventId": "match-100",
                                              "eventName": "Real Madrid vs Barcelona",
                                              "eventWinnerId": "REAL_MADRID"
                                            }
                                            """
                            )
                    )
            )
            EventOutcomeRequestDto request
    ) {
        log.info("Received request: {}", request);
        handler.handle(mapper.toDomain(request));
        return ResponseEntity.status(ACCEPTED).build();
    }

}
