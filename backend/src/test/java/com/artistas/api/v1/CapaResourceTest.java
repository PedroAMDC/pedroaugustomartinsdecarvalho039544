package com.artistas.api.v1;

import com.artistas.models.Album;
import com.artistas.models.Artista;
import com.artistas.models.CapaAlbum;
import com.artistas.models.TipoArtista;
import com.artistas.models.Usuario;
import com.artistas.services.RateLimitService;
import com.artistas.services.StorageService;
import com.artistas.services.TokenService;
import com.artistas.testresources.MinioTestResource;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
@QuarkusTestResource(MinioTestResource.class)
public class CapaResourceTest {

    @Inject
    TokenService tokenService;

    @Inject
    RateLimitService rateLimitService;

    @Inject
    StorageService storageService;

    private static final String TEST_EMAIL = "caparesource@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    private String accessToken;
    private Long albumId;
    private Long artistaId;
    private Long capaId;
    private String capaMinioKey;

    @BeforeEach
    void setUp() {
        rateLimitService.clearAll();

        QuarkusTransaction.requiringNew().run(() -> {
            Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
            if (usuario == null) {
                usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
                usuario.persist();
            }
        });

        accessToken = QuarkusTransaction.requiringNew().call(() -> {
            Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
            return tokenService.generateTokenPair(usuario).accessToken();
        });

        artistaId = QuarkusTransaction.requiringNew().call(() -> {
            Artista artista = Artista.create("Test Artist for Capa", TipoArtista.CANTOR);
            artista.persist();
            return artista.id;
        });

        albumId = QuarkusTransaction.requiringNew().call(() -> {
            Artista artista = Artista.findById(artistaId);
            Album album = Album.create("Test Album for Capa", 2020);
            album.artistas.add(artista);
            artista.albuns.add(album);
            album.persist();
            return album.id;
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            CapaAlbum.find("album.id", albumId).stream().forEach(c -> {
                CapaAlbum capa = (CapaAlbum) c;
                try {
                    storageService.deleteFile(capa.minioKey);
                } catch (Exception ignored) {
                }
                capa.delete();
            });
        });

        QuarkusTransaction.requiringNew().run(() -> {
            Album.find("titulo like ?1", "Test%").stream().forEach(a -> {
                Album album = (Album) a;
                for (Artista artista : album.artistas) {
                    artista.albuns.remove(album);
                }
                album.artistas.clear();
                album.delete();
            });
            Artista.delete("nome like ?1", "Test%");
            Usuario.delete("email", TEST_EMAIL);
        });

        if (capaMinioKey != null) {
            try {
                storageService.deleteFile(capaMinioKey);
            } catch (Exception ignored) {
            }
            capaMinioKey = null;
        }
    }

    @Test
    void upload_withValidImage_shouldReturn201() throws IOException {
        File testImage = createTestImageFile("test-image.png");

        try {
            given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testImage, "image/png")
            .when()
                .post("/v1/albuns/{albumId}/capas", albumId)
            .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("minioKey", notNullValue())
                .body("originalName", containsString("test-image.png"))
                .body("contentType", equalTo("image/png"))
                .body("tamanhoBytes", notNullValue());
        } finally {
            testImage.delete();
        }
    }

    @Test
    void upload_withJpegImage_shouldReturn201() throws IOException {
        File testImage = createTestImageFile("test-image.jpg");

        try {
            given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testImage, "image/jpeg")
            .when()
                .post("/v1/albuns/{albumId}/capas", albumId)
            .then()
                .statusCode(201)
                .body("contentType", equalTo("image/jpeg"));
        } finally {
            testImage.delete();
        }
    }

    @Test
    void upload_withoutToken_shouldReturn401() throws IOException {
        File testImage = createTestImageFile("test-image.png");

        try {
            given()
                .multiPart("file", testImage, "image/png")
            .when()
                .post("/v1/albuns/{albumId}/capas", albumId)
            .then()
                .statusCode(401);
        } finally {
            testImage.delete();
        }
    }

    @Test
    void upload_withInvalidFileType_shouldReturn400() throws IOException {
        File testFile = createTestImageFile("test-file.pdf");

        try {
            given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testFile, "application/pdf")
            .when()
                .post("/v1/albuns/{albumId}/capas", albumId)
            .then()
                .statusCode(400);
        } finally {
            testFile.delete();
        }
    }

    @Test
    void upload_withNonExistentAlbum_shouldReturn404() throws IOException {
        File testImage = createTestImageFile("test-image.png");

        try {
            given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testImage, "image/png")
            .when()
                .post("/v1/albuns/{albumId}/capas", 999999L)
            .then()
                .statusCode(404);
        } finally {
            testImage.delete();
        }
    }

    @Test
    void getPresignedUrl_withValidIds_shouldReturn200AndUrl() throws IOException {
        uploadTestCapa();

        given()
        .when()
            .get("/v1/albuns/{albumId}/capas/{capaId}/url", albumId, capaId)
        .then()
            .statusCode(200)
            .body("url", notNullValue())
            .body("url", startsWith("http"))
            .body("expiresInSeconds", equalTo(1800));
    }

    @Test
    void getPresignedUrl_withNonExistentCapa_shouldReturn404() {
        given()
        .when()
            .get("/v1/albuns/{albumId}/capas/{capaId}/url", albumId, 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    void getPresignedUrl_withNonExistentAlbum_shouldReturn404() throws IOException {
        uploadTestCapa();

        given()
        .when()
            .get("/v1/albuns/{albumId}/capas/{capaId}/url", 999999L, capaId)
        .then()
            .statusCode(404);
    }

    @Test
    void getPresignedUrl_withCapaFromDifferentAlbum_shouldReturn404() throws IOException {
        uploadTestCapa();

        Long otherAlbumId = QuarkusTransaction.requiringNew().call(() -> {
            Artista artista = Artista.findById(artistaId);
            Album album = Album.create("Test Other Album", 2021);
            album.artistas.add(artista);
            artista.albuns.add(album);
            album.persist();
            return album.id;
        });

        try {
            given()
            .when()
                .get("/v1/albuns/{albumId}/capas/{capaId}/url", otherAlbumId, capaId)
            .then()
                .statusCode(404);
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                Album album = Album.findById(otherAlbumId);
                if (album != null) {
                    for (Artista artista : album.artistas) {
                        artista.albuns.remove(album);
                    }
                    album.artistas.clear();
                    album.delete();
                }
            });
        }
    }

    @Test
    void delete_withValidToken_shouldReturn204() throws IOException {
        uploadTestCapa();

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/v1/albuns/{albumId}/capas/{capaId}", albumId, capaId)
        .then()
            .statusCode(204);

        given()
        .when()
            .get("/v1/albuns/{albumId}/capas/{capaId}/url", albumId, capaId)
        .then()
            .statusCode(404);
    }

    @Test
    void delete_withoutToken_shouldReturn401() throws IOException {
        uploadTestCapa();

        given()
        .when()
            .delete("/v1/albuns/{albumId}/capas/{capaId}", albumId, capaId)
        .then()
            .statusCode(401);
    }

    @Test
    void delete_withNonExistentCapa_shouldReturn404() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/v1/albuns/{albumId}/capas/{capaId}", albumId, 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    void delete_withNonExistentAlbum_shouldReturn404() throws IOException {
        uploadTestCapa();

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/v1/albuns/{albumId}/capas/{capaId}", 999999L, capaId)
        .then()
            .statusCode(404);
    }

    private void uploadTestCapa() throws IOException {
        File testImage = createTestImageFile("test-cover.png");

        try {
            capaId = given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testImage, "image/png")
            .when()
                .post("/v1/albuns/{albumId}/capas", albumId)
            .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

            capaMinioKey = QuarkusTransaction.requiringNew().call(() -> {
                CapaAlbum capa = CapaAlbum.findById(capaId);
                return capa != null ? capa.minioKey : null;
            });
        } finally {
            testImage.delete();
        }
    }

    private File createTestImageFile(String filename) throws IOException {
        File tempFile = File.createTempFile("test-", "-" + filename);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] minimalPng = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xFF, (byte) 0xFF, 0x3F,
                0x00, 0x05, (byte) 0xFE, 0x02, (byte) 0xFE, (byte) 0xDC, (byte) 0xCC, 0x59,
                (byte) 0xE7, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
                0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };
            fos.write(minimalPng);
        }
        return tempFile;
    }
}
