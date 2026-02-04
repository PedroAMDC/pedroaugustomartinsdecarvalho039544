package com.artistas.schemas;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Rate limit exceeded error response")
public class RateLimitErrorResponse {

    @Schema(description = "Error type", examples = {"Too Many Requests"})
    public String error;

    @Schema(description = "Detailed error message", examples = {"Rate limit exceeded. Try again in 60 seconds."})
    public String message;

    @Schema(description = "Seconds to wait before retrying", examples = {"60"})
    public Integer retryAfter;
}
