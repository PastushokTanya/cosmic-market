package com.tpastushok.cosmocats.annotation;

import com.tpastushok.cosmocats.featuretoggle.FeatureToggles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TurnFeatureToggleOn {
    FeatureToggles value();
}
