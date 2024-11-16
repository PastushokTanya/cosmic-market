package com.tpastushok.cosmocats.service.exception;

public class ThirdPartyServiceException extends RuntimeException{
    public ThirdPartyServiceException(String s) {
        super(s);
    }

    public ThirdPartyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
