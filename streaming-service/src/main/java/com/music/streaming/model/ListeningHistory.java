package com.music.streaming.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "listening_history")
public class ListeningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();

    public ListeningHistory() {
    }

    public ListeningHistory(Long userId, Long trackId) {
        this.userId = userId;
        this.trackId = trackId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTrackId() { return trackId; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}
