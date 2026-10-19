package com.music.playlists.dto;

import com.music.playlists.client.TrackInfo;
import com.music.playlists.model.Playlist;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistDetails(Long id, String name, String description, Long userId, LocalDateTime createdAt,
                              int trackCount, int totalDurationSec, List<TrackInfo> tracks) {

    public static PlaylistDetails of(Playlist p, List<TrackInfo> tracks) {
        int total = tracks.stream().filter(TrackInfo::available).mapToInt(TrackInfo::durationSec).sum();
        return new PlaylistDetails(p.getId(), p.getName(), p.getDescription(), p.getUserId(), p.getCreatedAt(),
                tracks.size(), total, tracks);
    }
}
