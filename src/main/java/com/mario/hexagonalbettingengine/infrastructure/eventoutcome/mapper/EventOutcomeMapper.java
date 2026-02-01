package com.mario.hexagonalbettingengine.infrastructure.eventoutcome.mapper;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import com.mario.hexagonalbettingengine.infrastructure.eventoutcome.payload.EventOutcomePayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventOutcomeMapper {
    EventOutcomePayload toPayload(EventOutcome eventOutcome);
    EventOutcome toDomain(EventOutcomePayload payload);
}
