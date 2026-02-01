package com.mario.hexagonalbettingengine.infrastructure.eventoutcome;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import org.mapstruct.Mapper;

@Mapper
public interface EventOutcomeMapper {

    EventOutcomePayload toPayload(EventOutcome eventOutcome);
}
