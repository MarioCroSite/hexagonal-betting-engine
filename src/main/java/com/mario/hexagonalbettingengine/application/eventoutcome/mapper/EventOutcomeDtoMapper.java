package com.mario.hexagonalbettingengine.application.eventoutcome.mapper;

import com.mario.hexagonalbettingengine.application.eventoutcome.request.EventOutcomeRequestDto;
import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventOutcomeDtoMapper {
    EventOutcome toDomain(EventOutcomeRequestDto request);
}
