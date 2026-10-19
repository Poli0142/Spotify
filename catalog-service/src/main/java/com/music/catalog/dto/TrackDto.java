package com.music.catalog.dto;

import com.music.catalog.model.Track;

public record TrackDto(Long id, String title, Integer durationSec, Long albumId, String albumTitle,
                       Long artistId, String artistName, String soundcloudUrl) {

    public static TrackDto from(Track t) {
        return new TrackDto(t.getId(), t.getTitle(), t.getDurationSec(),
                t.getAlbum().getId(), t.getAlbum().getTitle(),
                t.getAlbum().getArtist().getId(), t.getAlbum().getArtist().getName(), t.getSoundcloudUrl());
    }
}
