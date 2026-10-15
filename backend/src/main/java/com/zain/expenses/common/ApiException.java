package com.zain.expenses.common;

import org.springframework.http.HttpStatus;

/** An error that should reach the client with a specific HTTP status and message. */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
