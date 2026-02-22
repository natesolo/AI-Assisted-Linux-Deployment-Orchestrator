package com.nathan.aidlo.security;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class OutputRedactionService {

    private static final Pattern[] SECRET_PATTERNS = new Pattern[]{
            Pattern.compile("(?i)(password|passwd|pwd|token|api[-_]?key|secret)\\s*[=:]\\s*\\S+"),
            Pattern.compile("-----BEGIN [A-Z ]*PRIVATE KEY-----.*?-----END [A-Z ]*PRIVATE KEY-----", Pattern.DOTALL),
            Pattern.compile("(?i)authorization:\\s*bearer\\s+\\S+")
    };

    public String redact(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String redacted = value;
        for (Pattern pattern : SECRET_PATTERNS) {
            redacted = pattern.matcher(redacted).replaceAll("[REDACTED]");
        }
        return redacted;
    }
}
