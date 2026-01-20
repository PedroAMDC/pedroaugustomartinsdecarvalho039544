package com.artistas.services;

import com.artistas.models.Regional;
import com.artistas.schemas.RegionalApiResponse;
import com.artistas.schemas.RegionalSyncResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RegionalSyncService {

    @Inject
    @RestClient
    RegionalApiClient apiClient;

    @Transactional
    public RegionalSyncResponse sync() {
        List<RegionalApiResponse> externalList = apiClient.getRegionais();
        List<Regional> dbAtivos = Regional.findAllAtivos();

        Map<Integer, Regional> dbMap = dbAtivos.stream()
            .collect(Collectors.toMap(r -> r.externalId, r -> r));

        Set<Integer> externalIds = externalList.stream()
            .map(r -> r.id)
            .collect(Collectors.toCollection(HashSet::new));

        int inserted = 0;
        int inactivated = 0;
        int updated = 0;

        for (RegionalApiResponse ext : externalList) {
            Regional existing = dbMap.get(ext.id);

            if (existing == null) {
                Regional.create(ext.id, ext.nome).persist();
                inserted++;
            } else if (!existing.nome.equals(ext.nome)) {
                existing.ativo = false;
                Regional.create(ext.id, ext.nome).persist();
                updated++;
            }
        }

        for (Regional db : dbAtivos) {
            if (!externalIds.contains(db.externalId)) {
                db.ativo = false;
                inactivated++;
            }
        }

        return RegionalSyncResponse.of(inserted, inactivated, updated, externalList.size());
    }
}
