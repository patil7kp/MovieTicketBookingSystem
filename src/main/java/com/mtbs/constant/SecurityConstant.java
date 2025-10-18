package com.mtbs.constant;

public class SecurityConstant {

	public static final String ADMIN_AUTHORIZED_ACCESS = "hasRole('ADMIN')";

	public static final String CUSTOMER_AUTHORIZED_ACCESS = "hasRole('CUSTOMER')";

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
	

}
