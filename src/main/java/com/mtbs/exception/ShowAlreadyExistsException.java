package com.mtbs.exception;

@SuppressWarnings("all")
public class ShowAlreadyExistsException extends RuntimeException {
	public ShowAlreadyExistsException(String message) {
		super(message);
	}
}
