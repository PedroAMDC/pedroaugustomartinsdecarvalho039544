package com.artistas.schemas;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Regional synchronization result")
public class RegionalSyncResponse {

    @Schema(description = "Number of new regionais inserted", examples = {"3"})
    public Integer inserted;

    @Schema(description = "Number of regionais inactivated", examples = {"1"})
    public Integer inactivated;

    @Schema(description = "Number of regionais updated", examples = {"2"})
    public Integer updated;

    @Schema(description = "Total regionais after sync", examples = {"10"})
    public Integer total;

    public static RegionalSyncResponse of(Integer inserted, Integer inactivated, Integer updated, Integer total) {
        RegionalSyncResponse response = new RegionalSyncResponse();
        response.inserted = inserted;
        response.inactivated = inactivated;
        response.updated = updated;
        response.total = total;
        return response;
    }
}
