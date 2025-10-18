package com.mtbs.exception;

@SuppressWarnings("all")
public class InvalidShowRequestException extends RuntimeException {
	public InvalidShowRequestException(String message) {
		super(message);
	}
}
