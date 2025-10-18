package com.mtbs.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("all")
public class MovieException extends RuntimeException {

    private final HttpStatus status;

    public MovieException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
