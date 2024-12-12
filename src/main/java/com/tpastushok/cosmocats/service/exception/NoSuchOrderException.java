package com.tpastushok.cosmocats.service.exception;

public class NoSuchOrderException extends IllegalArgumentException {

    private static final String ERROR_MESSAGE_PATTERN = "Order with id: %s not found.";

    public NoSuchOrderException(String orderId) {
        super(String.format(ERROR_MESSAGE_PATTERN, orderId));
    }

    public NoSuchOrderException(String orderId, Throwable cause) {
        super(String.format(ERROR_MESSAGE_PATTERN, orderId), cause);
    }
}
