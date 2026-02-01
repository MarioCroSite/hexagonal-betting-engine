package com.mario.hexagonalbettingengine.infrastructure.betting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetJpaRepository extends JpaRepository<BetEntity, String> {
    List<BetEntity> findByEventIdAndStatus(String eventId, BetStatus status);
}
