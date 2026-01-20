package com.artistas.schemas;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AlbumRequest {

    @NotBlank
    public String titulo;

    @NotNull
    @Min(1900)
    @Max(2100)
    public Integer anoLancamento;

    @NotEmpty
    public List<Long> artistaIds;
}
