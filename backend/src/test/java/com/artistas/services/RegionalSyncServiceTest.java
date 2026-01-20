package com.artistas.services;

import com.artistas.models.Regional;
import com.artistas.schemas.RegionalApiResponse;
import com.artistas.schemas.RegionalSyncResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class RegionalSyncServiceTest {

    @Inject
    RegionalSyncService syncService;

    @InjectMock
    @RestClient
    RegionalApiClient apiClient;

    @BeforeEach
    @Transactional
    void setUp() {
        Regional.deleteAll();
    }

    private RegionalApiResponse createApiResponse(Integer id, String nome) {
        RegionalApiResponse response = new RegionalApiResponse();
        response.id = id;
        response.nome = nome;
        return response;
    }

    @Test
    @Transactional
    void sync_withNewRegionais_shouldInsert() {
        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA"),
            createApiResponse(2, "REGIONAL DE RONDONOPOLIS")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(2, result.inserted);
        assertEquals(0, result.inactivated);
        assertEquals(0, result.updated);
        assertEquals(2, result.total);

        List<Regional> allRegionais = Regional.listAll();
        assertEquals(2, allRegionais.size());
    }

    @Test
    @Transactional
    void sync_withRemovedRegional_shouldInactivate() {
        Regional existing = Regional.create(1, "REGIONAL DE CUIABA");
        existing.persist();

        List<RegionalApiResponse> externalList = Collections.emptyList();
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(0, result.inserted);
        assertEquals(1, result.inactivated);
        assertEquals(0, result.updated);
        assertEquals(0, result.total);

        Regional inactivated = Regional.findById(existing.id);
        assertFalse(inactivated.ativo);
    }

    @Test
    @Transactional
    void sync_withChangedNome_shouldInactivateOldAndCreateNew() {
        Regional existing = Regional.create(1, "REGIONAL DE CUIABA");
        existing.persist();

        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA - ALTERADA")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(0, result.inserted);
        assertEquals(0, result.inactivated);
        assertEquals(1, result.updated);
        assertEquals(1, result.total);

        List<Regional> allWithExternalId = Regional.findByExternalIdAll(1);
        assertEquals(2, allWithExternalId.size());

        long activeCount = allWithExternalId.stream().filter(r -> r.ativo).count();
        long inactiveCount = allWithExternalId.stream().filter(r -> !r.ativo).count();
        assertEquals(1, activeCount);
        assertEquals(1, inactiveCount);

        Regional activeRegional = Regional.findByExternalId(1);
        assertEquals("REGIONAL DE CUIABA - ALTERADA", activeRegional.nome);
    }

    @Test
    @Transactional
    void sync_withNoChanges_shouldNotModifyAnything() {
        Regional existing = Regional.create(1, "REGIONAL DE CUIABA");
        existing.persist();

        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(0, result.inserted);
        assertEquals(0, result.inactivated);
        assertEquals(0, result.updated);
        assertEquals(1, result.total);

        List<Regional> allRegionais = Regional.listAll();
        assertEquals(1, allRegionais.size());
        assertTrue(allRegionais.get(0).ativo);
    }

    @Test
    @Transactional
    void sync_withEmptyExternalResponse_shouldInactivateAll() {
        Regional existing1 = Regional.create(1, "REGIONAL DE CUIABA");
        existing1.persist();
        Regional existing2 = Regional.create(2, "REGIONAL DE RONDONOPOLIS");
        existing2.persist();

        when(apiClient.getRegionais()).thenReturn(Collections.emptyList());

        RegionalSyncResponse result = syncService.sync();

        assertEquals(0, result.inserted);
        assertEquals(2, result.inactivated);
        assertEquals(0, result.updated);
        assertEquals(0, result.total);

        List<Regional> allRegionais = Regional.listAll();
        assertTrue(allRegionais.stream().noneMatch(r -> r.ativo));
    }

    @Test
    @Transactional
    void sync_withMixedOperations_shouldHandleAllCases() {
        Regional toKeep = Regional.create(1, "REGIONAL DE CUIABA");
        toKeep.persist();
        Regional toInactivate = Regional.create(2, "REGIONAL DE RONDONOPOLIS");
        toInactivate.persist();
        Regional toUpdate = Regional.create(3, "REGIONAL DE SINOP");
        toUpdate.persist();

        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA"),
            createApiResponse(3, "REGIONAL DE SINOP - NOVA"),
            createApiResponse(4, "REGIONAL DE TANGARA DA SERRA")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(1, result.inserted);
        assertEquals(1, result.inactivated);
        assertEquals(1, result.updated);
        assertEquals(3, result.total);
    }

    @Test
    void sync_whenApiFails_shouldThrowException() {
        when(apiClient.getRegionais()).thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> syncService.sync());
    }

    @Test
    @Transactional
    void sync_withInactiveRegionalReappearing_shouldCreateNew() {
        Regional inactive = Regional.create(1, "REGIONAL DE CUIABA", false);
        inactive.persist();

        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        RegionalSyncResponse result = syncService.sync();

        assertEquals(1, result.inserted);
        assertEquals(0, result.inactivated);
        assertEquals(0, result.updated);

        List<Regional> allWithExternalId = Regional.findByExternalIdAll(1);
        assertEquals(2, allWithExternalId.size());

        Regional activeOne = Regional.findByExternalId(1);
        assertNotNull(activeOne);
        assertTrue(activeOne.ativo);
    }
}
