package com.mario.hexagonalbettingengine.infrastructure.betting.mapper;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetEntity;
import com.mario.hexagonalbettingengine.infrastructure.betting.payload.BetPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface BetMapper {
    Bet toDomain(BetEntity entity);
    BetEntity toEntity(Bet domain);

    @Mapping(target = "settledAt", expression = "java(Instant.now())")
    BetPayload toPayload(Bet bet);
}
