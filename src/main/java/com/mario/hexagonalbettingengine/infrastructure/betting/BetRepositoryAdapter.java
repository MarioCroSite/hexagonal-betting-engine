package com.mario.hexagonalbettingengine.infrastructure.betting;

import com.mario.hexagonalbettingengine.domain.betting.Bet;
import com.mario.hexagonalbettingengine.domain.betting.BetRepository;
import com.mario.hexagonalbettingengine.infrastructure.betting.mapper.BetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BetRepositoryAdapter implements BetRepository {

    private final BetJpaRepository repository;
    private final BetMapper mapper;

    @Override
    public List<Bet> findPendingBetsByEventId(String eventId) {
        var entities = repository.findByEventIdAndStatus(eventId, BetStatus.PENDING);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public void save(Bet bet) {
        var entity = mapper.toEntity(bet);
        repository.save(entity);
    }
}
