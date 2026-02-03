package com.artistas.schemas;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Error message", examples = {"Resource not found"})
    public String error;
}
