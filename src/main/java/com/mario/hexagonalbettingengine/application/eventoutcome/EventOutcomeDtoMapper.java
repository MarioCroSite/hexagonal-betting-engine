package com.mario.hexagonalbettingengine.application.eventoutcome;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import org.mapstruct.Mapper;

@Mapper
public interface EventOutcomeDtoMapper {
    EventOutcome toDomain(EventOutcomeRequestDto request);
}
