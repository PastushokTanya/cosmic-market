package com.tpastushok.cosmocats.dto.validation;

/**
 * Marker interface used to define an additional validation group for advanced validation checks.
 *
 * ExtendedValidation is utilized in @GroupSequence annotations to specify a multi-stage
 * validation sequence. This allows certain validation constraints to be applied only after
 * basic validation passes. For example, in CustomerDetailsDto, basic validation (e.g.,
 * @NotBlank, @Size) is performed first, and then extended validation (e.g., @ValidSpaceAddress)
 * is only applied if the primary validations succeed.
 *
 * By using this interface without inheritance or implementation, we can manage validation
 * groups flexibly without affecting the class hierarchy, keeping validation logic modular.
 */
public class ExtendedValidation {
}
