INSERT INTO bets (bet_id, user_id, event_id, event_market_id, event_winner_id, bet_amount, status)
VALUES
-- Event 1: Real Madrid vs Barcelona (match-100)
('b-001', 'user-1', 'match-100', '1x2', 'REAL_MADRID', 10.00, 'PENDING'),
('b-002', 'user-2', 'match-100', '1x2', 'BARCELONA', 25.50, 'PENDING'),
('b-003', 'user-3', 'match-100', '1x2', 'DRAW', 5.00, 'PENDING'),
('b-004', 'user-4', 'match-100', '1x2', 'REAL_MADRID', 100.00, 'PENDING'),
-- Event 2: Liverpool vs Milan (match-200)
('b-005', 'user-1', 'match-200', '1x2', 'LIVERPOOL', 15.00, 'PENDING'),
('b-006', 'user-5', 'match-200', '1x2', 'MILAN', 40.00, 'PENDING'),
('b-007', 'user-2', 'match-200', '1x2', 'LIVERPOOL', 12.00, 'PENDING'),
-- Event 3: Lakers vs Celtics (match-300)
('b-008', 'user-6', 'match-300', 'winner', 'LAKERS', 50.00, 'PENDING'),
('b-009', 'user-7', 'match-300', 'winner', 'CELTICS', 30.00, 'PENDING'),
('b-010', 'user-1', 'match-300', 'winner', 'LAKERS', 20.00, 'PENDING');