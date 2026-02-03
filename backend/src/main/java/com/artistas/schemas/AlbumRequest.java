package com.artistas.schemas;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Album creation/update request")
public class AlbumRequest {

    @NotBlank
    @Schema(description = "Album title", examples = {"Dois"})
    public String titulo;

    @NotNull
    @Min(1900)
    @Max(2100)
    @Schema(description = "Release year", examples = {"1986"})
    public Integer anoLancamento;

    @NotEmpty
    @Schema(description = "List of artist IDs associated with the album", examples = {"[1, 2]"})
    public List<Long> artistaIds;
}
