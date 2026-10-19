package com.music.playlists.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public PlaylistTrack() {
    }

    public PlaylistTrack(Long playlistId, Long trackId) {
        this.playlistId = playlistId;
        this.trackId = trackId;
    }

    public Long getId() { return id; }
    public Long getPlaylistId() { return playlistId; }
    public Long getTrackId() { return trackId; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
