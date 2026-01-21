package com.artistas.filters;

import com.artistas.services.RateLimitService;
import com.artistas.services.RateLimitService.RateLimitInfo;
import com.artistas.services.exceptions.RateLimitException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServerRequest httpServerRequest;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JsonWebToken jwt;

    @BeforeEach
    void setUp() throws Exception {
        Field requestField = RateLimitFilter.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(rateLimitFilter, httpServerRequest);
    }

    @Test
    void checkRateLimit_shouldApplyToV1Endpoints() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("192.168.1.1");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:192.168.1.1")).thenReturn(allowedInfo);

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService).checkAndIncrement("ip:192.168.1.1");
        verify(requestContext).setProperty(eq("rateLimitInfo"), any(RateLimitInfo.class));
    }

    @Test
    void checkRateLimit_shouldApplyToV1EndpointsWithLeadingSlash() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/api/v1/albums");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("10.0.0.1");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:10.0.0.1")).thenReturn(allowedInfo);

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService).checkAndIncrement("ip:10.0.0.1");
    }

    @Test
    void checkRateLimit_shouldNotApplyToNonV1Endpoints() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/health");

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService, never()).checkAndIncrement(anyString());
    }

    @Test
    void checkRateLimit_shouldNotApplyToSwaggerEndpoints() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/openapi");

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService, never()).checkAndIncrement(anyString());
    }

    @Test
    void checkRateLimit_shouldExtractUserIdFromJwt() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(123L);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("user:123")).thenReturn(allowedInfo);

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService).checkAndIncrement("user:123");
    }

    @Test
    void checkRateLimit_shouldFallbackToIpWhenNoJwt() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("192.168.1.100");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:192.168.1.100")).thenReturn(allowedInfo);

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService).checkAndIncrement("ip:192.168.1.100");
    }

    @Test
    void checkRateLimit_shouldUseXForwardedForHeader() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178");

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:203.0.113.195")).thenReturn(allowedInfo);

        rateLimitFilter.checkRateLimit(requestContext);

        verify(rateLimitService).checkAndIncrement("ip:203.0.113.195");
    }

    @Test
    void checkRateLimit_shouldThrowExceptionWhenLimitExceeded() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("192.168.1.1");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo exceededInfo = new RateLimitInfo(false, 10, 0, System.currentTimeMillis() + 30000, 30);
        when(rateLimitService.checkAndIncrement("ip:192.168.1.1")).thenReturn(exceededInfo);

        RateLimitException exception = assertThrows(RateLimitException.class, () -> {
            rateLimitFilter.checkRateLimit(requestContext);
        });

        assertEquals(30, exception.getRetryAfterSeconds());
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }

    @Test
    void checkRateLimit_shouldAllowRequestsWithinLimit() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(456L);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 5, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("user:456")).thenReturn(allowedInfo);

        assertDoesNotThrow(() -> rateLimitFilter.checkRateLimit(requestContext));
        verify(requestContext).setProperty("rateLimitInfo", allowedInfo);
    }

    @Test
    void addRateLimitHeaders_shouldAddHeadersWhenInfoPresent() {
        long resetTimestamp = System.currentTimeMillis() + 60000;
        RateLimitInfo info = new RateLimitInfo(true, 10, 7, resetTimestamp, 0);
        when(requestContext.getProperty("rateLimitInfo")).thenReturn(info);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        rateLimitFilter.addRateLimitHeaders(requestContext, responseContext);

        assertEquals(10, headers.getFirst("X-RateLimit-Limit"));
        assertEquals(7, headers.getFirst("X-RateLimit-Remaining"));
        assertEquals(resetTimestamp / 1000, headers.getFirst("X-RateLimit-Reset"));
    }

    @Test
    void addRateLimitHeaders_shouldNotAddHeadersWhenInfoAbsent() {
        when(requestContext.getProperty("rateLimitInfo")).thenReturn(null);

        rateLimitFilter.addRateLimitHeaders(requestContext, responseContext);

        verify(responseContext, never()).getHeaders();
    }

    @Test
    void checkRateLimit_shouldHandleNullSecurityContext() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("127.0.0.1");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:127.0.0.1")).thenReturn(allowedInfo);

        assertDoesNotThrow(() -> rateLimitFilter.checkRateLimit(requestContext));
        verify(rateLimitService).checkAndIncrement("ip:127.0.0.1");
    }

    @Test
    void checkRateLimit_shouldHandleNullRemoteAddress() throws Exception {
        Field requestField = RateLimitFilter.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(rateLimitFilter, httpServerRequest);

        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServerRequest.remoteAddress()).thenReturn(null);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:unknown")).thenReturn(allowedInfo);

        assertDoesNotThrow(() -> rateLimitFilter.checkRateLimit(requestContext));
        verify(rateLimitService).checkAndIncrement("ip:unknown");
    }

    @Test
    void checkRateLimit_shouldHandleJwtWithNullUserId() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/artistas");
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(null);
        when(httpServerRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("192.168.0.1");
        when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);

        RateLimitInfo allowedInfo = new RateLimitInfo(true, 10, 9, System.currentTimeMillis() + 60000, 0);
        when(rateLimitService.checkAndIncrement("ip:192.168.0.1")).thenReturn(allowedInfo);

        assertDoesNotThrow(() -> rateLimitFilter.checkRateLimit(requestContext));
        verify(rateLimitService).checkAndIncrement("ip:192.168.0.1");
    }
}
