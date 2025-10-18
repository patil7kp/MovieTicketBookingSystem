package com.mtbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

	@NotBlank(message = "Username cannot be empty")
	@Size(min = 3, max = 20, message = "Username must be 3-20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can contain letters, numbers, '.', '_', or '-' only")
	private String username;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 8, message = "Password must be at least 8 characters")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$", message = "Password must contain uppercase, lowercase, digit, and special character")
	private String password;

	private String role;


}
