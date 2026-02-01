package com.mario.hexagonalbettingengine.domain.betting;

import java.util.List;

public interface BetRepository {
    List<Bet> findPendingBetsByEventId(String eventId);
    void save(Bet bet);
}
