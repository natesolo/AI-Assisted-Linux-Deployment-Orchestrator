package com.nathan.aidlo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private final ApiKeySecurityProperties properties;

    public ApiKeyAuthInterceptor(ApiKeySecurityProperties properties) {
        this.properties = properties;
        if (properties.enabled() && (properties.value() == null || properties.value().isBlank())) {
            throw new IllegalStateException("API key auth is enabled but aidlo.security.api-key.value is empty");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.enabled()) {
            return true;
        }

        String provided = request.getHeader(properties.headerName());
        if (provided != null && provided.equals(properties.value())) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Unauthorized\"}");
        return false;
    }
}
