package com.yofujitsu.booking_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yofujitsu.booking_service.exception.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorDto body = new ErrorDto(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication is required",
                request.getRequestURI(),
                MDC.get("requestId")
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
