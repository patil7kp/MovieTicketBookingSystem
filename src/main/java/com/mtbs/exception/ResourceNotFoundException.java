package com.mtbs.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("all")
public class ResourceNotFoundException extends RuntimeException {
	String resourceName;
	String fieldName;
	Long fieldValue;

	public ResourceNotFoundException(String resourceName, String fieldName, long fieldValue) {
		super(String.format("%s not found with %s : %s", resourceName, fieldName, fieldValue));
		this.resourceName = resourceName;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	// Security Exception
	public ResourceNotFoundException(String resourceName, String fieldName) {
		super(String.format("%s not found Email Id : %s", resourceName, fieldName));
		this.resourceName = resourceName;
		this.fieldName = fieldName;
	}

}
