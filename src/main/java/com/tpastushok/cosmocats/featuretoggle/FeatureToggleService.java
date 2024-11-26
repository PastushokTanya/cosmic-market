package com.tpastushok.cosmocats.featuretoggle;

import com.tpastushok.cosmocats.config.FeatureToggleProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FeatureToggleService {
    private final Map<String, Boolean> toggles;

    public FeatureToggleService(FeatureToggleProperties props) {
        toggles = new HashMap<>(props.getToggles());
    }

    public boolean enabled(String name) {
        return toggles.getOrDefault(name, false);
    }

    public void turnOn(String name) {
        toggles.put(name, true);
    }

    public void turnOff(String name) {
        toggles.put(name, false);
    }
}
