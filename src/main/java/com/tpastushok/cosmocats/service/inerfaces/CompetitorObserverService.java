package com.tpastushok.cosmocats.service.inerfaces;

import com.tpastushok.cosmocats.dto.competitors.observer.CompetitorsObserverResponseDto;

import java.util.UUID;

public interface CompetitorObserverService {
    CompetitorsObserverResponseDto observeOtherStorePrices(UUID productId);
}
