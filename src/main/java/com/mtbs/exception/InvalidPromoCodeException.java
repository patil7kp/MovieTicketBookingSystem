package com.mtbs.exception;

@SuppressWarnings("all")
public class InvalidPromoCodeException extends RuntimeException {
	public InvalidPromoCodeException(String message) {
		super(message);
	}
}
