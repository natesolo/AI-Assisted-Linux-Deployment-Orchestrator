package com.nathan.aidlo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private final ApiKeyAuthInterceptor apiKeyAuthInterceptor;
    private final DeploymentRateLimitInterceptor deploymentRateLimitInterceptor;

    public WebSecurityConfig(
            ApiKeyAuthInterceptor apiKeyAuthInterceptor,
            DeploymentRateLimitInterceptor deploymentRateLimitInterceptor
    ) {
        this.apiKeyAuthInterceptor = apiKeyAuthInterceptor;
        this.deploymentRateLimitInterceptor = deploymentRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyAuthInterceptor)
                .addPathPatterns("/api/**")
                .addPathPatterns("/actuator/**");

        registry.addInterceptor(deploymentRateLimitInterceptor)
                .addPathPatterns("/api/v1/deployments");
    }
}
