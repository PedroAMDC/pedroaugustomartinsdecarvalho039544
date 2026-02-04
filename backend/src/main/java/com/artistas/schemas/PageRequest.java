package com.artistas.schemas;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class PageRequest {

    @QueryParam("page")
    @DefaultValue("0")
    public Integer page;

    @QueryParam("size")
    @DefaultValue("10")
    public Integer size;

    @QueryParam("sort")
    public String sort;

    @QueryParam("direction")
    @DefaultValue("ASC")
    public String direction;

    public int getOffset() {
        return page * size;
    }
}
