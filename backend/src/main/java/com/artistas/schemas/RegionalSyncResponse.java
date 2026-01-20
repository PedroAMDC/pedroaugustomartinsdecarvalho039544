package com.artistas.schemas;

public class RegionalSyncResponse {

    public Integer inserted;
    public Integer inactivated;
    public Integer updated;
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
