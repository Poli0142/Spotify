package com.music.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TrackRequest(
        @Schema(example = "Новый трек") @NotBlank String title,
        @Schema(example = "210") @NotNull @Positive Integer durationSec,
        @Schema(example = "1") @NotNull Long albumId,
        @Schema(example = "https://soundcloud.com/code80-472176323/blue-7") String soundcloudUrl) {
}
