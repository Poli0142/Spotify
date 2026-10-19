package com.music.streaming.dto;

import com.music.streaming.client.TrackInfo;

import java.time.LocalDateTime;

public record PlayResponse(Long id, Long userId, TrackInfo track, LocalDateTime playedAt, boolean degraded) {
}
