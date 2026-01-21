package com.artistas.services;

import com.artistas.models.Album;
import com.artistas.models.CapaAlbum;
import com.artistas.schemas.CapaAlbumResponse;
import com.artistas.schemas.CapaPresignedUrlResponse;
import com.artistas.services.exceptions.NotFoundException;
import com.artistas.services.exceptions.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.io.IOException;
import java.nio.file.Files;

@ApplicationScoped
public class CapaService {

    private static final int PRESIGNED_URL_EXPIRY_SECONDS = 1800;

    @Inject
    StorageService storageService;

    @Transactional
    public CapaAlbumResponse upload(Long albumId, FileUpload file) {
        validateFileUpload(file);
        Album album = findAlbumOrThrow(albumId);

        String objectKey = uploadToStorage(file);
        CapaAlbum capa = createAndPersistCapa(album, file, objectKey);

        return CapaAlbumResponse.of(capa);
    }

    public CapaPresignedUrlResponse getPresignedUrl(Long albumId, Long capaId) {
        CapaAlbum capa = findCapaOrThrow(albumId, capaId);
        String url = storageService.generatePresignedUrl(capa.minioKey);
        return CapaPresignedUrlResponse.of(url, PRESIGNED_URL_EXPIRY_SECONDS);
    }

    @Transactional
    public void delete(Long albumId, Long capaId) {
        CapaAlbum capa = findCapaOrThrow(albumId, capaId);
        storageService.deleteFile(capa.minioKey);
        capa.delete();
    }

    private Album findAlbumOrThrow(Long albumId) {
        Album album = Album.findById(albumId);
        if (album == null) {
            throw new NotFoundException("Album not found");
        }
        return album;
    }

    private CapaAlbum findCapaOrThrow(Long albumId, Long capaId) {
        findAlbumOrThrow(albumId);
        CapaAlbum capa = CapaAlbum.findById(capaId);
        if (capa == null || !capa.album.id.equals(albumId)) {
            throw new NotFoundException("Cover not found");
        }
        return capa;
    }

    private void validateFileUpload(FileUpload file) {
        if (file == null || file.fileName() == null) {
            throw new ValidationException("File is required");
        }
        storageService.validateImageType(file.contentType());
    }

    private String uploadToStorage(FileUpload file) {
        try {
            return storageService.uploadFile(
                Files.newInputStream(file.filePath()),
                file.fileName(),
                file.contentType(),
                file.size()
            );
        } catch (IOException e) {
            throw new ValidationException("Failed to read uploaded file");
        }
    }

    private CapaAlbum createAndPersistCapa(Album album, FileUpload file, String objectKey) {
        CapaAlbum capa = CapaAlbum.create(
            album,
            objectKey,
            file.fileName(),
            file.contentType(),
            file.size()
        );
        capa.persist();
        return capa;
    }
}
