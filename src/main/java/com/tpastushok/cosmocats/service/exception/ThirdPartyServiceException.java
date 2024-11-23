package com.tpastushok.cosmocats.service.exception;

import org.springframework.http.HttpStatusCode;

public class ThirdPartyServiceException extends RuntimeException {

    private static final String ERROR_MESSAGE_PATTERN = "Third-party service error (HttpStatus: %s): %s";

    public ThirdPartyServiceException(HttpStatusCode status, String detailedMessage) {
        super(String.format(ERROR_MESSAGE_PATTERN, status, detailedMessage));
    }

    public ThirdPartyServiceException(HttpStatusCode status, String detailedMessage, Throwable cause) {
        super(String.format(ERROR_MESSAGE_PATTERN, status, detailedMessage), cause);
    }
}
