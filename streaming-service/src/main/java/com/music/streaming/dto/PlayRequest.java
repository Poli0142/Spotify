package com.music.streaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PlayRequest(
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "4") @NotNull Long trackId) {
}
