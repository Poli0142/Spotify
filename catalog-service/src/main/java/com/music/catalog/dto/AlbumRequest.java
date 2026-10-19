package com.music.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlbumRequest(
        @Schema(example = "Новый сингл") @NotBlank String title,
        @Schema(example = "2025") @NotNull @Min(1900) @Max(2100) Integer releaseYear,
        @Schema(example = "1") @NotNull Long artistId) {
}
