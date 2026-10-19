CREATE TABLE artists (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    genre   VARCHAR(50)  NOT NULL,
    country VARCHAR(50),
    user_id BIGINT UNIQUE
);

CREATE TABLE albums (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    release_year INT          NOT NULL CHECK (release_year BETWEEN 1900 AND 2100),
    artist_id    BIGINT       NOT NULL REFERENCES artists (id) ON DELETE CASCADE
);

CREATE TABLE tracks (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    duration_sec   INT          NOT NULL CHECK (duration_sec > 0),
    soundcloud_url VARCHAR(255),
    album_id       BIGINT       NOT NULL REFERENCES albums (id) ON DELETE CASCADE
);

CREATE INDEX idx_albums_artist ON albums (artist_id);
CREATE INDEX idx_tracks_album  ON tracks (album_id);

INSERT INTO artists (name, genre, country, user_id) VALUES
    ('CODE10', 'Хип-хоп', NULL, NULL),
    ('CODE80', 'Хип-хоп', NULL, 2);

INSERT INTO albums (title, release_year, artist_id) VALUES
    ('I Feel Like Dying', 2024, 1),
    ('JUICE',             2024, 1),
    ('Lamb',              2024, 1),
    ('NewL',              2024, 2),
    ('SummerLOVE',        2024, 2),
    ('Blue',              2024, 2),
    ('Champ',             2024, 2);

INSERT INTO tracks (title, duration_sec, soundcloud_url, album_id) VALUES
    ('I Feel Like Dying', 107, 'https://soundcloud.com/code80-472176323/i-feel-like-dying',        1),
    ('JUICE',             157, 'https://soundcloud.com/yn8/code10juice',                           2),
    ('Lamb',              108, 'https://soundcloud.com/s7a7ic/code10-lamb-og-version',             3),
    ('NewL',              145, 'https://soundcloud.com/prodam-last-475641358/code80-newl-3',       4),
    ('SummerLOVE',        180, 'https://soundcloud.com/1dead1/code10-summerlove',                  5),
    ('Blue',              134, 'https://soundcloud.com/code80-472176323/blue-7',                   6),
    ('Champ',             177, 'https://soundcloud.com/scalevillain/champ-hexd-ver',               7);
