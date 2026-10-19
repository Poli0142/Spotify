package com.music.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ArtistRequest(
        @Schema(example = "Новый исполнитель") @NotBlank String name,
        @Schema(example = "Хип-хоп") @NotBlank String genre,
        @Schema(example = "Россия") String country) {
}
