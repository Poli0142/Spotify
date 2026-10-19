package com.music.streaming.dto;

import com.music.streaming.client.TrackInfo;

public record TopTrack(TrackInfo track, long plays) {
}
