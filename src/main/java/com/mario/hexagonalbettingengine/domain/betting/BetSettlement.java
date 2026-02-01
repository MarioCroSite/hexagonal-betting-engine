package com.mario.hexagonalbettingengine.domain.betting;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;

public interface BetSettlement {
    void settle(EventOutcome eventOutcome);
}
