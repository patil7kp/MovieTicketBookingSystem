package com.mtbs.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("all")
public class UserRegisterException extends RuntimeException {

    private final HttpStatus status;

    public UserRegisterException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
