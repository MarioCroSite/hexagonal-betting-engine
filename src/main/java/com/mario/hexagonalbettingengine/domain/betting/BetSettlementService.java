package com.mario.hexagonalbettingengine.domain.betting;

import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.WON;
import static com.mario.hexagonalbettingengine.domain.betting.BetStatus.LOST;

import com.mario.hexagonalbettingengine.domain.eventoutcome.EventOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetSettlementService implements BetSettlement {

    private final BetRepository betRepository;

    @Override
    @Transactional
    public void settle(EventOutcome eventOutcome) {
        log.info("Starting settlement for Event ID: {}", eventOutcome.eventId());

        var pendingBets = betRepository.findPendingBetsByEventId(eventOutcome.eventId());

        if (pendingBets.isEmpty()) {
            log.info("No pending bets found for event {}", eventOutcome.eventId());
            return;
        }

        log.info("Found {} bets to process", pendingBets.size());
        pendingBets.forEach(bet -> processSingleBet(bet, eventOutcome.eventWinnerId()));
    }

    private void processSingleBet(Bet bet, String actualWinnerId) {
        var isWinner = bet.isWinner(actualWinnerId);
        var finalStatus = isWinner ? WON : LOST;

        var settledBet = bet.withStatus(finalStatus);
        betRepository.save(settledBet);
    }
}
