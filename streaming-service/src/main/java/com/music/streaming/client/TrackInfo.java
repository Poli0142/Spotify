package com.music.streaming.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackInfo(Long id, String title, String artistName, String albumTitle, Integer durationSec,
                        String soundcloudUrl, Boolean available) {

    public static TrackInfo unavailable(Long id) {
        return new TrackInfo(id, null, null, null, null, null, false);
    }
}
