package com.artistas.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@ApplicationScoped
public class RateLimitService {

    private static final long WINDOW_SIZE_SECONDS = 60;

    @ConfigProperty(name = "rate-limit.requests-per-minute", defaultValue = "10")
    int maxRequestsPerMinute;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestLog = new ConcurrentHashMap<>();

    public record RateLimitInfo(
        boolean allowed,
        int limit,
        int remaining,
        long resetTimestamp,
        int retryAfterSeconds
    ) {}

    public RateLimitInfo checkAndIncrement(String identifier) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);

        ConcurrentLinkedDeque<Long> timestamps = requestLog.computeIfAbsent(
            identifier,
            k -> new ConcurrentLinkedDeque<>()
        );

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }

            int currentCount = timestamps.size();

            if (currentCount >= maxRequestsPerMinute) {
                Long oldestTimestamp = timestamps.peekFirst();
                int retryAfter = oldestTimestamp != null
                    ? (int) Math.ceil((oldestTimestamp + WINDOW_SIZE_SECONDS * 1000 - now) / 1000.0)
                    : (int) WINDOW_SIZE_SECONDS;

                long resetTimestamp = oldestTimestamp != null
                    ? oldestTimestamp + WINDOW_SIZE_SECONDS * 1000
                    : now + WINDOW_SIZE_SECONDS * 1000;

                return new RateLimitInfo(
                    false,
                    maxRequestsPerMinute,
                    0,
                    resetTimestamp,
                    Math.max(1, retryAfter)
                );
            }

            timestamps.addLast(now);

            return new RateLimitInfo(
                true,
                maxRequestsPerMinute,
                maxRequestsPerMinute - currentCount - 1,
                now + WINDOW_SIZE_SECONDS * 1000,
                0
            );
        }
    }

    public int getRemainingRequests(String identifier) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);

        ConcurrentLinkedDeque<Long> timestamps = requestLog.get(identifier);

        if (timestamps == null) {
            return maxRequestsPerMinute;
        }

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            return Math.max(0, maxRequestsPerMinute - timestamps.size());
        }
    }

    public int getLimit() {
        return maxRequestsPerMinute;
    }

    public void clearAll() {
        requestLog.clear();
    }
}
