package com.artistas.services;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RateLimitServiceTest {

    @Inject
    RateLimitService rateLimitService;

    private static final String TEST_USER_1 = "user:1";
    private static final String TEST_USER_2 = "user:2";

    @BeforeEach
    void setUp() {
        rateLimitService.clearAll();
    }

    @Test
    void checkAndIncrement_firstRequest_shouldAllow() {
        RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);

        assertTrue(info.allowed());
        assertEquals(10, info.limit());
        assertEquals(9, info.remaining());
        assertEquals(0, info.retryAfterSeconds());
    }

    @Test
    void checkAndIncrement_tenthRequest_shouldAllow() {
        for (int i = 0; i < 9; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
        }

        RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);

        assertTrue(info.allowed());
        assertEquals(10, info.limit());
        assertEquals(0, info.remaining());
    }

    @Test
    void checkAndIncrement_eleventhRequest_shouldReject() {
        for (int i = 0; i < 10; i++) {
            RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);
            assertTrue(info.allowed(), "Request " + (i + 1) + " should be allowed");
        }

        RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);

        assertFalse(info.allowed());
        assertEquals(10, info.limit());
        assertEquals(0, info.remaining());
        assertTrue(info.retryAfterSeconds() > 0);
        assertTrue(info.retryAfterSeconds() <= 60);
    }

    @Test
    void checkAndIncrement_differentUsers_shouldTrackSeparately() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
        }

        RateLimitService.RateLimitInfo user1Info = rateLimitService.checkAndIncrement(TEST_USER_1);
        assertFalse(user1Info.allowed());

        RateLimitService.RateLimitInfo user2Info = rateLimitService.checkAndIncrement(TEST_USER_2);
        assertTrue(user2Info.allowed());
        assertEquals(9, user2Info.remaining());
    }

    @Test
    void checkAndIncrement_shouldReturnCorrectRetryAfter() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
        }

        RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);

        assertFalse(info.allowed());
        assertTrue(info.retryAfterSeconds() >= 1);
        assertTrue(info.retryAfterSeconds() <= 60);
        assertTrue(info.resetTimestamp() > System.currentTimeMillis());
    }

    @Test
    void getRemainingRequests_noRequests_shouldReturnLimit() {
        int remaining = rateLimitService.getRemainingRequests("new-user");

        assertEquals(10, remaining);
    }

    @Test
    void getRemainingRequests_afterSomeRequests_shouldReturnCorrectRemaining() {
        rateLimitService.checkAndIncrement(TEST_USER_1);
        rateLimitService.checkAndIncrement(TEST_USER_1);
        rateLimitService.checkAndIncrement(TEST_USER_1);

        int remaining = rateLimitService.getRemainingRequests(TEST_USER_1);

        assertEquals(7, remaining);
    }

    @Test
    void getLimit_shouldReturnConfiguredLimit() {
        int limit = rateLimitService.getLimit();

        assertEquals(10, limit);
    }

    @Test
    void clearAll_shouldResetAllLimits() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
        }

        assertFalse(rateLimitService.checkAndIncrement(TEST_USER_1).allowed());

        rateLimitService.clearAll();

        RateLimitService.RateLimitInfo info = rateLimitService.checkAndIncrement(TEST_USER_1);
        assertTrue(info.allowed());
        assertEquals(9, info.remaining());
    }

    @Test
    void checkAndIncrement_multipleConcurrentUsers_shouldTrackIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
            rateLimitService.checkAndIncrement(TEST_USER_2);
        }

        assertEquals(5, rateLimitService.getRemainingRequests(TEST_USER_1));
        assertEquals(5, rateLimitService.getRemainingRequests(TEST_USER_2));

        for (int i = 0; i < 5; i++) {
            rateLimitService.checkAndIncrement(TEST_USER_1);
        }

        assertFalse(rateLimitService.checkAndIncrement(TEST_USER_1).allowed());
        assertTrue(rateLimitService.checkAndIncrement(TEST_USER_2).allowed());
    }
}
