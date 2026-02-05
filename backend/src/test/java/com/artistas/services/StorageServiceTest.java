package com.artistas.services;

import com.artistas.services.exceptions.StorageException;
import com.artistas.services.exceptions.ValidationException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @Mock
    MinioClient minioClient;

    @InjectMocks
    StorageService storageService;

    private static final String TEST_BUCKET = "test-bucket";
    private static final int TEST_EXPIRY_SECONDS = 1800;

    private MinioClient presignedUrlClient;

    @BeforeEach
    void setUp() throws Exception {
        presignedUrlClient = mock(MinioClient.class);
        setField(storageService, "bucket", TEST_BUCKET);
        setField(storageService, "presignedUrlExpirySeconds", TEST_EXPIRY_SECONDS);
        setField(storageService, "presignedUrlClient", presignedUrlClient);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void uploadFile_withValidImage_shouldReturnObjectKey() throws Exception {
        byte[] content = "test image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);
        String filename = "test-image.jpg";
        String contentType = "image/jpeg";
        long size = content.length;

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        String objectKey = storageService.uploadFile(inputStream, filename, contentType, size);

        assertNotNull(objectKey);
        assertTrue(objectKey.endsWith("_test-image.jpg"));

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        PutObjectArgs capturedArgs = captor.getValue();
        assertEquals(TEST_BUCKET, capturedArgs.bucket());
        assertEquals(contentType, capturedArgs.contentType());
    }

    @Test
    void uploadFile_withPngImage_shouldReturnObjectKey() throws Exception {
        byte[] content = "png content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        String objectKey = storageService.uploadFile(inputStream, "image.png", "image/png", content.length);

        assertNotNull(objectKey);
        assertTrue(objectKey.contains("_image.png"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_withGifImage_shouldReturnObjectKey() throws Exception {
        byte[] content = "gif content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        String objectKey = storageService.uploadFile(inputStream, "animation.gif", "image/gif", content.length);

        assertNotNull(objectKey);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_withWebpImage_shouldReturnObjectKey() throws Exception {
        byte[] content = "webp content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        String objectKey = storageService.uploadFile(inputStream, "photo.webp", "image/webp", content.length);

        assertNotNull(objectKey);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_withInvalidContentType_shouldThrowValidationException() {
        byte[] content = "pdf content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> storageService.uploadFile(inputStream, "document.pdf", "application/pdf", content.length)
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
        verifyNoInteractions(minioClient);
    }

    @Test
    void uploadFile_withNullContentType_shouldThrowValidationException() {
        byte[] content = "content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> storageService.uploadFile(inputStream, "file.txt", null, content.length)
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void uploadFile_withTextPlainContentType_shouldThrowValidationException() {
        byte[] content = "text content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        assertThrows(
            ValidationException.class,
            () -> storageService.uploadFile(inputStream, "file.txt", "text/plain", content.length)
        );
    }

    @Test
    void uploadFile_whenMinioFails_shouldThrowStorageException() throws Exception {
        byte[] content = "image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        when(minioClient.putObject(any(PutObjectArgs.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        StorageException exception = assertThrows(
            StorageException.class,
            () -> storageService.uploadFile(inputStream, "image.jpg", "image/jpeg", content.length)
        );

        assertTrue(exception.getMessage().contains("Failed to upload file"));
    }

    @Test
    void generatePresignedUrl_withValidObjectKey_shouldReturnUrl() throws Exception {
        String objectKey = "uuid_test-image.jpg";
        String expectedUrl = "http://localhost:9000/test-bucket/uuid_test-image.jpg?token=abc123";

        when(presignedUrlClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
            .thenReturn(expectedUrl);

        String url = storageService.generatePresignedUrl(objectKey);

        assertEquals(expectedUrl, url);

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(presignedUrlClient).getPresignedObjectUrl(captor.capture());

        GetPresignedObjectUrlArgs capturedArgs = captor.getValue();
        assertEquals(TEST_BUCKET, capturedArgs.bucket());
        assertEquals(objectKey, capturedArgs.object());
    }

    @Test
    void generatePresignedUrl_whenMinioFails_shouldThrowStorageException() throws Exception {
        when(presignedUrlClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        StorageException exception = assertThrows(
            StorageException.class,
            () -> storageService.generatePresignedUrl("some-key")
        );

        assertTrue(exception.getMessage().contains("Failed to generate presigned URL"));
    }

    @Test
    void deleteFile_withValidObjectKey_shouldCallRemoveObject() throws Exception {
        String objectKey = "uuid_test-image.jpg";

        storageService.deleteFile(objectKey);

        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());

        RemoveObjectArgs capturedArgs = captor.getValue();
        assertEquals(TEST_BUCKET, capturedArgs.bucket());
        assertEquals(objectKey, capturedArgs.object());
    }

    @Test
    void deleteFile_whenMinioFails_shouldThrowStorageException() throws Exception {
        doThrow(new RuntimeException("Connection refused"))
            .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        StorageException exception = assertThrows(
            StorageException.class,
            () -> storageService.deleteFile("some-key")
        );

        assertTrue(exception.getMessage().contains("Failed to delete file"));
    }

    @Test
    void validateImageType_withJpeg_shouldNotThrow() {
        assertDoesNotThrow(() -> storageService.validateImageType("image/jpeg"));
    }

    @Test
    void validateImageType_withPng_shouldNotThrow() {
        assertDoesNotThrow(() -> storageService.validateImageType("image/png"));
    }

    @Test
    void validateImageType_withGif_shouldNotThrow() {
        assertDoesNotThrow(() -> storageService.validateImageType("image/gif"));
    }

    @Test
    void validateImageType_withWebp_shouldNotThrow() {
        assertDoesNotThrow(() -> storageService.validateImageType("image/webp"));
    }

    @Test
    void validateImageType_withUpperCase_shouldNotThrow() {
        assertDoesNotThrow(() -> storageService.validateImageType("IMAGE/JPEG"));
    }

    @Test
    void validateImageType_withInvalidType_shouldThrowValidationException() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> storageService.validateImageType("application/octet-stream")
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
        assertTrue(exception.getMessage().contains("image/jpeg"));
    }

    @Test
    void uploadFile_shouldSanitizeFilename() throws Exception {
        byte[] content = "image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);
        String unsafeFilename = "my file@#$%.jpg";

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        String objectKey = storageService.uploadFile(inputStream, unsafeFilename, "image/jpeg", content.length);

        assertNotNull(objectKey);
        assertFalse(objectKey.contains("@"));
        assertFalse(objectKey.contains("#"));
        assertFalse(objectKey.contains("$"));
        assertFalse(objectKey.contains("%"));
        assertTrue(objectKey.contains(".jpg"));
    }
}
