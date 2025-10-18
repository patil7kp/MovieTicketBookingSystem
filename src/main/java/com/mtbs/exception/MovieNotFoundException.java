package com.mtbs.exception;

@SuppressWarnings("all")
public class MovieNotFoundException extends RuntimeException {
	public MovieNotFoundException(String message) {
		super(message);
	}
}
