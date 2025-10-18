package com.mtbs.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("all")
public class ApiSecurityException extends RuntimeException {

	private String message;

	public ApiSecurityException(String message) {
		super(message);
		this.message = message;
	}

}
