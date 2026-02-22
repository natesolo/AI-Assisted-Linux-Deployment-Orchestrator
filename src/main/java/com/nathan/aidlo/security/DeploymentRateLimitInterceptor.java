package com.nathan.aidlo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeploymentRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitSecurityProperties properties;
    private final ApiKeySecurityProperties apiKeySecurityProperties;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public DeploymentRateLimitInterceptor(RateLimitSecurityProperties properties, ApiKeySecurityProperties apiKeySecurityProperties) {
        this.properties = properties;
        this.apiKeySecurityProperties = apiKeySecurityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.enabled()) {
            return true;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod()) || !"/api/v1/deployments".equals(request.getRequestURI())) {
            return true;
        }

        long now = Instant.now().getEpochSecond();
        String key = requestKey(request);
        Window current = windows.compute(key, (k, existing) -> updateWindow(existing, now));

        if (current.count() > properties.maxRequests()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded for deployment submissions\"}");
            return false;
        }

        return true;
    }

    private String requestKey(HttpServletRequest request) {
        String apiKey = request.getHeader(apiKeySecurityProperties.headerName());
        if (apiKey != null && !apiKey.isBlank()) {
            return "api:" + apiKey;
        }
        return "ip:" + request.getRemoteAddr();
    }

    private Window updateWindow(Window existing, long now) {
        if (existing == null || now - existing.windowStartEpochSeconds() >= properties.windowSeconds()) {
            return new Window(now, 1);
        }
        return new Window(existing.windowStartEpochSeconds(), existing.count() + 1);
    }

    private record Window(long windowStartEpochSeconds, int count) {
    }
}
