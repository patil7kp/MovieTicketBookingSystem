package com.mtbs.exception;

@SuppressWarnings("all")
public class SeatUnavailableException extends RuntimeException {
	public SeatUnavailableException(String message) {
		super(message);
	}
}
