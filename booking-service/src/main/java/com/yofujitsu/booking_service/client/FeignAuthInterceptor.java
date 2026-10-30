package com.yofujitsu.booking_service.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            template.header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + jwtAuthentication.getToken().getTokenValue()
            );
        }

        String requestId = MDC.get("requestId");
        if (StringUtils.hasText(requestId)) {
            template.header("X-Request-Id", requestId);
        }
    }
}
