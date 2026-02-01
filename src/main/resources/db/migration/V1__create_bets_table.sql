CREATE TABLE IF NOT EXISTS bets (
    bet_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_market_id VARCHAR(255) NOT NULL,
    event_winner_id VARCHAR(255) NOT NULL,
    bet_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,

    PRIMARY KEY (bet_id)
);

CREATE INDEX IF NOT EXISTS idx_bets_event_status ON bets (event_id, status);