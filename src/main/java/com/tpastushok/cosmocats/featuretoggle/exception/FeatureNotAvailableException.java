package com.tpastushok.cosmocats.featuretoggle.exception;

public class FeatureNotAvailableException extends IllegalArgumentException {
    public static final String ERROR_MESSAGE_PATTERN = "Sorry, but feature \"%s\" is not available. Try again later)";

    public FeatureNotAvailableException(String featureName) {
        super(String.format(ERROR_MESSAGE_PATTERN, featureName));
    }
}
