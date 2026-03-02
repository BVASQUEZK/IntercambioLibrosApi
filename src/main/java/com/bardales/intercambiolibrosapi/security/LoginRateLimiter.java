package com.bardales.intercambiolibrosapi.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final class AttemptWindow {
        private int attempts;
        private long windowStartMs;
        private long blockedUntilMs;
    }

    private final ConcurrentMap<String, AttemptWindow> buckets = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long windowMs;
    private final long blockMs;

    public LoginRateLimiter(
            @Value("${security.login-rate-limit.max-attempts:8}") int maxAttempts,
            @Value("${security.login-rate-limit.window-ms:900000}") long windowMs,
            @Value("${security.login-rate-limit.block-ms:900000}") long blockMs) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.windowMs = Math.max(Duration.ofMinutes(1).toMillis(), windowMs);
        this.blockMs = Math.max(Duration.ofMinutes(1).toMillis(), blockMs);
    }

    public boolean isBlocked(String key) {
        AttemptWindow bucket = buckets.get(normalizeKey(key));
        if (bucket == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        synchronized (bucket) {
            if (bucket.blockedUntilMs <= now) {
                if (bucket.windowStartMs + windowMs <= now) {
                    buckets.remove(normalizeKey(key), bucket);
                }
                return false;
            }
            return true;
        }
    }

    public long secondsUntilRelease(String key) {
        AttemptWindow bucket = buckets.get(normalizeKey(key));
        if (bucket == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        synchronized (bucket) {
            if (bucket.blockedUntilMs <= now) {
                return 0;
            }
            return Math.max(1, (bucket.blockedUntilMs - now + 999) / 1000);
        }
    }

    public void registerFailure(String key) {
        String normalized = normalizeKey(key);
        long now = System.currentTimeMillis();
        AttemptWindow bucket = buckets.computeIfAbsent(normalized, k -> {
            AttemptWindow created = new AttemptWindow();
            created.windowStartMs = now;
            return created;
        });
        synchronized (bucket) {
            if (bucket.blockedUntilMs > now) {
                return;
            }
            if (bucket.windowStartMs + windowMs <= now) {
                bucket.windowStartMs = now;
                bucket.attempts = 0;
            }
            bucket.attempts++;
            if (bucket.attempts >= maxAttempts) {
                bucket.blockedUntilMs = now + blockMs;
                bucket.attempts = 0;
                bucket.windowStartMs = now;
            }
        }
    }

    public void reset(String key) {
        buckets.remove(normalizeKey(key));
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return "unknown";
        }
        return key.trim().toLowerCase();
    }
}
