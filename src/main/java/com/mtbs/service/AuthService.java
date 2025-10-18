package com.mtbs.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mtbs.dto.UserRegisterDTO;
import com.mtbs.entity.User;
import com.mtbs.exception.AuthenticationException;
import com.mtbs.exception.UserRegisterException;
import com.mtbs.repository.UserRepository;
import com.mtbs.response.LoginResponse;
import com.mtbs.security.JwtTokenHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenHelper jwtUtil;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    
    
    public Map<String, Object> registerUserDetails(UserRegisterDTO userDto) {

        if (userDto == null) {
            throw new UserRegisterException("User details cannot be null", HttpStatus.BAD_REQUEST);
        }

        String username = userDto.getUsername() != null ? userDto.getUsername().trim() : null;
        String password = userDto.getPassword() != null ? userDto.getPassword().trim() : null;

        if (username == null || username.isEmpty()) {
            throw new UserRegisterException("Username cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserRegisterException("Username already taken", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);

        // Fix: only set role once
        if (userDto.getRole() == null) {
            user.setRole("CUSTOMER");   // default role
        } else if (!isValidRole(userDto.getRole())) {
            throw new UserRegisterException("Invalid role provided", HttpStatus.BAD_REQUEST);
        } else {
            user.setRole(userDto.getRole());
        }

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully user registered");
        response.put("username", username);

        return response;
    }

    private boolean isValidRole(String role) {
        return role.equals("ADMIN") || role.equals("CUSTOMER");
    }

    
    
    
    public LoginResponse loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String genrateToken = jwtUtil.genrateToken(userDetails);

        return new LoginResponse(
                user.getUsername(),
                user.getRole(),
                genrateToken,
                "Login successful"
        );
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}

