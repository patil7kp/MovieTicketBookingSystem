package com.mtbs.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mtbs.exception.ApiSecurityException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenHelper jwtTokenHelper;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtTokenHelper jwtTokenHelper) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenHelper = jwtTokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestToken = request.getHeader("Authorization");
        log.info("Requested Token : {}", requestToken);

        String username = null;
        String actualToken = null;

        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            actualToken = requestToken.substring(7);

            try {
                username = this.jwtTokenHelper.getUsernameFromToken(actualToken);
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException :: username and password", e);
            } catch (ExpiredJwtException e) {
                log.error("ExpiredJwtException :: JWT Token has expired!!", e);
            } catch (MalformedJwtException e) {
                log.error("MalformedJwtException :: Invalid JWT Token", e);
            }

        } else {
            log.warn("JWT Token does not begin with 'Bearer ' prefix.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtTokenHelper.isValidateToken(actualToken, userDetails)) {

                // ✅ Extract role from token
                String role = jwtTokenHelper.getRoleFromToken(actualToken);
                log.info("Extracted Role from Token: {}", role);

                // ✅ Convert it to Spring authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ✅ Set authentication
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                log.error("JwtTokenNotValidException :: Token is not valid");
                throw new ApiSecurityException("Token is not valid !!");
            }
        }

        filterChain.doFilter(request, response);
    }
}
