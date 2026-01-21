package com.artistas.services;

import com.artistas.services.exceptions.ValidationException;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CapaServiceTest {

    @Mock
    StorageService storageService;

    @InjectMocks
    CapaService capaService;

    @Test
    void upload_withNullFile_shouldThrowValidationException() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> capaService.upload(1L, null)
        );

        assertEquals("File is required", exception.getMessage());
    }

    @Test
    void upload_withNullFilename_shouldThrowValidationException() {
        FileUpload mockFile = mock(FileUpload.class);
        when(mockFile.fileName()).thenReturn(null);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> capaService.upload(1L, mockFile)
        );

        assertEquals("File is required", exception.getMessage());
    }

    @Test
    void upload_withInvalidContentType_shouldThrowValidationException() {
        FileUpload mockFile = mock(FileUpload.class);
        when(mockFile.fileName()).thenReturn("document.pdf");
        when(mockFile.contentType()).thenReturn("application/pdf");

        doThrow(new ValidationException("Invalid file type"))
            .when(storageService).validateImageType("application/pdf");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> capaService.upload(1L, mockFile)
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
    }
}
