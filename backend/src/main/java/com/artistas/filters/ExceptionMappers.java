package com.artistas.filters;

import com.artistas.services.exceptions.AuthenticationException;
import com.artistas.services.exceptions.ConflictException;
import com.artistas.services.exceptions.ValidationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import java.util.Map;

public class ExceptionMappers {

    @ServerExceptionMapper
    public Response mapAuthenticationException(AuthenticationException ex) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", ex.getMessage()))
            .build();
    }

    @ServerExceptionMapper
    public Response mapValidationException(ValidationException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", ex.getMessage()))
            .build();
    }

    @ServerExceptionMapper
    public Response mapConflictException(ConflictException ex) {
        return Response.status(Response.Status.CONFLICT)
            .entity(Map.of("error", ex.getMessage()))
            .build();
    }
}
