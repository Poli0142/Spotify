package com.music.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(length = 50)
    private String country;

    @Column(name = "user_id", unique = true)
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
