package com.tpastushok.cosmocats.util;

import java.net.URI;
import java.util.List;

import com.tpastushok.cosmocats.service.exception.ParamsViolationDetails;
import lombok.experimental.UtilityClass;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@UtilityClass
public class ProductDetailsUtils {

    public static ProblemDetail getValidationErrorsProblemDetail(List<ParamsViolationDetails> validationResponse) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Request validation failed");

        // `type` specifies a unique identifier for the error type, here using a URN:
        // `urn:problem-type:validation-error`
        // - "urn" is a Uniform Resource Name, a type of identifier that provides a name
        //   for a resource without implying its location.
        // - "problem-type" is a custom namespace for categorizing different types of errors.
        // - "validation-error" designates this specific error type as related to validation.
        // Using a URN helps uniquely identify errors consistently and allows clients to
        // handle specific error types programmatically by checking the `type` field.
        problemDetail.setType(URI.create("urn:problem-type:validation-error"));
        problemDetail.setTitle("Field Validation Exception");
        problemDetail.setProperty("invalidParams", validationResponse);
        return problemDetail;
    }
}
