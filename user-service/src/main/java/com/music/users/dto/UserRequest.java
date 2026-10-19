package com.music.users.dto;

import com.music.users.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Schema(example = "petya") @NotBlank @Size(max = 50) String username,
        @Schema(example = "12345") @NotBlank @Size(min = 4, max = 50) String password,
        @Schema(example = "Петя") @NotBlank @Size(max = 100) String displayName,
        @Schema(example = "USER", description = "USER или ARTIST") Role role) {
}
