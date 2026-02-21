package com.nathan.aidlo.common;

import java.time.Instant;

public record ApiError(String message, Instant timestamp) {
}
