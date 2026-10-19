package com.music.playlists.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaylistRequest(
        @Schema(example = "Ночной вайб") @NotBlank String name,
        @Schema(example = "CODE10 на ночь") String description,
        @Schema(example = "1") @NotNull Long userId) {
}
