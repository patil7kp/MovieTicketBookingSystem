package com.mtbs.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mtbs.constant.PathConstant;
import com.mtbs.dto.UserRegisterDTO;
import com.mtbs.response.LoginRequest;
import com.mtbs.response.LoginResponse;
import com.mtbs.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathConstant.AUTH_PATH)
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping(PathConstant.AUTH_REGISTER)
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDTO dto) {
		Map<String, Object> createdUser = authService.registerUserDetails(dto);
		return ResponseEntity.ok(createdUser);
	}

	@PostMapping(PathConstant.AUTH_LOGIN)
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
		LoginResponse response = authService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
		return ResponseEntity.ok(response);
	}

}
