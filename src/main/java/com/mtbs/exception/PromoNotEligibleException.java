package com.mtbs.exception;

@SuppressWarnings("all")
public class PromoNotEligibleException extends RuntimeException {
	public PromoNotEligibleException(String message) {
		super(message);
	}
}
