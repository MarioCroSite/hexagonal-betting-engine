package com.mario.hexagonalbettingengine.infrastructure.betting.mapper;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.infrastructure.betting.BetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BetMapper {
    Bet toDomain(BetEntity entity);
    BetEntity toEntity(Bet domain);
}
