package com.music.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
}
