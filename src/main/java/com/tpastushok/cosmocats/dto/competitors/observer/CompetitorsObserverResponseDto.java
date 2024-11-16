package com.tpastushok.cosmocats.dto.competitors.observer;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class CompetitorsObserverResponseDto {
    UUID productUuid;
    List<CompetitorStorePrice> stores;
}

@Value
@Builder
@Jacksonized
class CompetitorStorePrice{
    String storeName;
    Double regularPrice;
    Double markedDownPrice;
}
