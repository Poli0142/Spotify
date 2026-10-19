package com.music.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "soundcloud_url")
    private String soundcloudUrl;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "album_id")
    private Album album;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public String getSoundcloudUrl() { return soundcloudUrl; }
    public void setSoundcloudUrl(String soundcloudUrl) { this.soundcloudUrl = soundcloudUrl; }
    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }
}
