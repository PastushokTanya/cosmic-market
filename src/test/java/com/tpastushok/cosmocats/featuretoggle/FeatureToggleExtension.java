package com.tpastushok.cosmocats.featuretoggle;

import com.tpastushok.cosmocats.annotation.TurnFeatureToggleOff;
import com.tpastushok.cosmocats.annotation.TurnFeatureToggleOn;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class FeatureToggleExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {

        context.getTestMethod().ifPresent(method -> {

            FeatureToggleService featureToggleService = getFeatureToggleService(context);

            if (method.isAnnotationPresent(TurnFeatureToggleOn.class)) {
                TurnFeatureToggleOn enabledFeatureToggleAnnotation = method.getAnnotation(TurnFeatureToggleOn.class);
                featureToggleService.turnOn(enabledFeatureToggleAnnotation.value().getName());
            } else if (method.isAnnotationPresent(TurnFeatureToggleOff.class)) {
                TurnFeatureToggleOff disabledFeatureToggleAnnotation = method.getAnnotation(TurnFeatureToggleOff.class);
                featureToggleService.turnOff(disabledFeatureToggleAnnotation.value().getName());
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestMethod().ifPresent(method -> {
            String featureName = null;

            if (method.isAnnotationPresent(TurnFeatureToggleOn.class)) {
                TurnFeatureToggleOn enabledFeatureToggleAnnotation = method.getAnnotation(TurnFeatureToggleOn.class);
                featureName = enabledFeatureToggleAnnotation.value().getName();
            } else if (method.isAnnotationPresent(TurnFeatureToggleOff.class)) {
                TurnFeatureToggleOff disabledFeatureToggleAnnotation = method.getAnnotation(TurnFeatureToggleOff.class);
                featureName = disabledFeatureToggleAnnotation.value().getName();
            }
            if (featureName != null) {
                FeatureToggleService featureToggleService = getFeatureToggleService(context);
                if (getFeatureNamePropertyAsBoolean(context, featureName)) {
                    featureToggleService.turnOn(featureName);
                } else {
                    featureToggleService.turnOff(featureName);
                }
            }
        });
    }

    private boolean getFeatureNamePropertyAsBoolean(ExtensionContext context, String featureName) {
        Environment environment = SpringExtension.getApplicationContext(context).getEnvironment();
        return environment.getProperty("application.feature.toggles." + featureName, Boolean.class, Boolean.FALSE);
    }

    private FeatureToggleService getFeatureToggleService(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context).getBean(FeatureToggleService.class);
    }
}
