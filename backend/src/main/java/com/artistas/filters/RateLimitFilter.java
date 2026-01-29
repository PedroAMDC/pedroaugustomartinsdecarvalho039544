package com.artistas.filters;

import com.artistas.services.RateLimitService;
import com.artistas.services.RateLimitService.RateLimitInfo;
import com.artistas.services.exceptions.RateLimitException;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import java.security.Principal;

public class RateLimitFilter {

    private static final String RATE_LIMIT_HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_HEADER_RESET = "X-RateLimit-Reset";
    private static final String RATE_LIMIT_INFO_PROPERTY = "rateLimitInfo";

    @Inject
    RateLimitService rateLimitService;

    @Context
    HttpServerRequest request;

    @ServerRequestFilter
    public void checkRateLimit(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (!shouldApplyRateLimit(path)) {
            return;
        }

        String identifier = extractUserIdentifier(requestContext);
        RateLimitInfo info = rateLimitService.checkAndIncrement(identifier);

        requestContext.setProperty(RATE_LIMIT_INFO_PROPERTY, info);

        if (!info.allowed()) {
            throw new RateLimitException(
                "Rate limit exceeded. Try again in " + info.retryAfterSeconds() + " seconds.",
                info.retryAfterSeconds()
            );
        }
    }

    @ServerResponseFilter
    public void addRateLimitHeaders(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        RateLimitInfo info = (RateLimitInfo) requestContext.getProperty(RATE_LIMIT_INFO_PROPERTY);

        if (info != null) {
            responseContext.getHeaders().add(RATE_LIMIT_HEADER_LIMIT, info.limit());
            responseContext.getHeaders().add(RATE_LIMIT_HEADER_REMAINING, info.remaining());
            responseContext.getHeaders().add(RATE_LIMIT_HEADER_RESET, info.resetTimestamp() / 1000);
        }
    }

    private String extractUserIdentifier(ContainerRequestContext context) {
        SecurityContext securityContext = context.getSecurityContext();

        if (securityContext != null) {
            Principal principal = securityContext.getUserPrincipal();
            if (principal instanceof JsonWebToken jwt) {
                Object userIdClaim = jwt.getClaim("userId");
                if (userIdClaim != null) {
                    return "user:" + userIdClaim;
                }
            }
        }

        return "ip:" + getClientIpAddress();
    }

    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.remoteAddress() != null ? request.remoteAddress().host() : "unknown";
    }

    private boolean shouldApplyRateLimit(String path) {
        return path.startsWith("v1/") || path.startsWith("/v1/");
    }
}
