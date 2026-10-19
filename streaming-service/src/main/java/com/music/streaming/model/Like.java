package com.music.streaming.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Like() {
    }

    public Like(Long userId, Long trackId) {
        this.userId = userId;
        this.trackId = trackId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTrackId() { return trackId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
