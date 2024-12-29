package com.tpastushok.cosmocats.web;

import com.tpastushok.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import com.tpastushok.cosmocats.service.exception.NoSuchOrderException;
import com.tpastushok.cosmocats.service.exception.NoSuchProductException;
import com.tpastushok.cosmocats.service.exception.PersistenceException;
import com.tpastushok.cosmocats.service.exception.ThirdPartyServiceException;
import com.tpastushok.cosmocats.util.ParamsViolationDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

import static com.tpastushok.cosmocats.util.ProductDetailsUtils.getValidationErrorsProblemDetail;
import static java.net.URI.create;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ProblemDetail.forStatusAndDetail;

/**
 * @ControllerAdvice annotation is used to define a global exception handler
 * across multiple controllers in the application.
 *
 * This class, ExceptionTranslator, handles exceptions thrown from any controller,
 * providing a centralized way to manage error responses. Using @ControllerAdvice
 * allows us to capture specific exceptions, like NoSuchProductException, and customize
 * the response to the client (e.g., setting HTTP status, message, and problem details).
 *
 * This approach improves code maintainability by keeping exception handling logic
 * separate from controller classes, and it can be easily expanded to handle other
 * exceptions as needed.
 */
@ControllerAdvice
@Slf4j
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PersistenceException.class)
    ProblemDetail handlePersistenceException(PersistenceException ex) {
        log.error("Persistence exception raised");
        ProblemDetail problemDetail = forStatusAndDetail(INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setType(create("persistence-exception"));
        problemDetail.setTitle("Persistence exception");
        return problemDetail;
    }

    @ExceptionHandler(NoSuchProductException.class)
    ProblemDetail handleStoreConfigurationNotFoundException(NoSuchProductException ex) {
        log.info("Product Not Found exception raised");
        ProblemDetail problemDetail = forStatusAndDetail(NOT_FOUND, ex.getMessage());
        problemDetail.setType(create("product-not-found"));
        problemDetail.setTitle("Product Not Found");
        return problemDetail;
    }
    @ExceptionHandler(NoSuchOrderException.class)
    ProblemDetail handleStoreConfigurationNotFoundException(NoSuchOrderException ex) {
        log.info("Order Not Found exception raised");
        ProblemDetail problemDetail = forStatusAndDetail(NOT_FOUND, ex.getMessage());
        problemDetail.setType(create("order-not-found"));
        problemDetail.setTitle("Order Not Found");
        return problemDetail;
    }

    @ExceptionHandler(ThirdPartyServiceException.class)
    ProblemDetail handleThirdPartyServiceException(ThirdPartyServiceException ex) {
        log.error("Third-party service exception: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = forStatusAndDetail(INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setType(create("third-party-service-error"));
        problemDetail.setTitle("Error communicating with third-party service");
        return problemDetail;
    }

    @ExceptionHandler(FeatureNotAvailableException.class)
    ProblemDetail handleFeatureToggleNotEnabledException(FeatureNotAvailableException ex) {
        log.error("Feature is not enabled");
        ProblemDetail problemDetail = forStatusAndDetail(NOT_FOUND, ex.getMessage());
        problemDetail.setType(create("feature-disabled"));
        problemDetail.setTitle("Feature is disabled");
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        List<ParamsViolationDetails> validationResponse =
                errors.stream().map(err -> ParamsViolationDetails.builder().reason(err.getDefaultMessage()).fieldName(err.getField()).build()).toList();
        log.info("Input params validation failed");
        return ResponseEntity.status(BAD_REQUEST).body(getValidationErrorsProblemDetail(validationResponse));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request) {
        log.error("Authorization denied: {}", ex.getMessage());
        ProblemDetail problemDetail = forStatusAndDetail(FORBIDDEN, "Access Denied");
        problemDetail.setType(create("authorization-denied"));
        problemDetail.setTitle("Forbidden");
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    ProblemDetail handleAuthenticationException(AuthenticationCredentialsNotFoundException ex, WebRequest request) {
        log.error("Authentication credentials not provided: {}", ex.getMessage());
        ProblemDetail problemDetail = forStatusAndDetail(UNAUTHORIZED, "No authentication credentials provided");
        problemDetail.setType(create("authentication-required"));
        problemDetail.setTitle("Authentication Required");
        return problemDetail;
    }
}
