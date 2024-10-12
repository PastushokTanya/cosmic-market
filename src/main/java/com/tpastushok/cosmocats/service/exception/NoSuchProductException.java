package com.tpastushok.cosmocats.service.exception;

public class NoSuchProductException extends IllegalArgumentException{
    public NoSuchProductException(String s) {
        super(s);
    }

    public NoSuchProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
