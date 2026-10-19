package com.music.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record PublishRequest(
        @Schema(example = "Blue") @NotBlank String title,
        @Schema(example = "2") @NotNull Long userId,
        @Schema(example = "CODE80") @NotBlank String artistName,
        @Schema(example = "Хип-хоп") @NotBlank String genre,
        @Schema(example = "https://soundcloud.com/code80-472176323/blue-7")
        @NotBlank @Pattern(regexp = "https://(www\\.|m\\.)?soundcloud\\.com/.+", message = "нужна ссылка на SoundCloud")
        String soundcloudUrl,
        @Schema(example = "134") @NotNull @Positive Integer durationSec) {
}
