package com.tpastushok.cosmocats.featuretoggle.aspect;

import com.tpastushok.cosmocats.featuretoggle.FeatureToggleService;
import com.tpastushok.cosmocats.featuretoggle.FeatureToggles;
import com.tpastushok.cosmocats.featuretoggle.annotation.FeatureToggle;
import com.tpastushok.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {
    private final FeatureToggleService featureToggleService;

    @Before(value = "@annotation(featureToggle)")
    public void throwExceptionIfFeatureIsDisabled(FeatureToggle featureToggle) {
        FeatureToggles toggle = featureToggle.value();

        if (!featureToggleService.enabled(toggle.getName())) {
            log.warn("Feature toggle '{}' is not enabled!", toggle.getName());
            throw new FeatureNotAvailableException(toggle.getName());
        }
    }
}
