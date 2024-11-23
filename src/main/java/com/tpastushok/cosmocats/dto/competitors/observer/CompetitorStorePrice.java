package com.tpastushok.cosmocats.dto.competitors.observer;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CompetitorStorePrice{
    String storeName;
    Double regularPrice;
    Double markedDownPrice;
}
