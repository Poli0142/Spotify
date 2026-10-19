CREATE TABLE playlists (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE playlist_tracks (
    id          BIGSERIAL PRIMARY KEY,
    playlist_id BIGINT    NOT NULL REFERENCES playlists (id) ON DELETE CASCADE,
    track_id    BIGINT    NOT NULL,
    added_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (playlist_id, track_id)
);

CREATE INDEX idx_playlists_user ON playlists (user_id);

INSERT INTO playlists (name, description, user_id) VALUES
    ('Мой вайб',   'Лучшее от CODE10 и CODE80', 1),
    ('На повторе', 'Слушаю каждый день',        1);

INSERT INTO playlist_tracks (playlist_id, track_id) VALUES
    (1, 1), (1, 3), (1, 4), (1, 6),
    (2, 2), (2, 5), (2, 7);
