package com.mario.hexagonalbettingengine.application.eventoutcome.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record EventOutcomeRequestDto(
        @NotBlank(message = "Event ID must not be blank")
        String eventId,
        @NotBlank(message = "Event Name must not be blank")
        String eventName,
        @NotBlank(message = "Event Winner ID must not be blank")
        String eventWinnerId) {

}
