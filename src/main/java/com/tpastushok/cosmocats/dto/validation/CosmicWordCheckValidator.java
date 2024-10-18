package com.tpastushok.cosmocats.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {

    private static final List<String> COSMIC_TERMS = Arrays.asList(
            "Anti-Gravity", "Cosmic", "Stardust", "Galaxy", "Nebula",
            "Meteor", "Asteroid", "Quasar", "Supernova", "Black Hole",
            "Wormhole", "Pulsar", "Comet", "Star", "Orbit", "Eclipse",
            "Celestial", "Constellation", "Interstellar", "Planetary",
            "Solar", "Lunar", "Astral", "Galactic", "Zenith"
    );

    private String message;

    @Override
    public void initialize(CosmicWordCheck constraintAnnotation) {
        // Replace {cosmicTerms} placeholder in message with actual list of terms
        String terms = getCosmicTermsDisplay();
        this.message = constraintAnnotation.message().replace("{cosmicTerms}", terms);
    }

    @Override
    public boolean isValid(String fieldValue, ConstraintValidatorContext context) {
        if (fieldValue == null) {
            return false; // If the field is null, it doesn't satisfy the requirement
        }

        // Case-insensitive check if the field contains any cosmic term
        boolean isValid = COSMIC_TERMS.stream()
                .anyMatch(term -> fieldValue.toLowerCase().contains(term.toLowerCase()));

        // Update the default message with the cosmic terms list
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(this.message)
                    .addConstraintViolation();
        }

        return isValid;
    }

    /**
     * Provides a single string of cosmic terms for display in error messages.
     */
    public static String getCosmicTermsDisplay() {
        return String.join(", ", COSMIC_TERMS);
    }
}
