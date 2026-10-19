CREATE TABLE listening_history (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    track_id  BIGINT    NOT NULL,
    played_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_history_user  ON listening_history (user_id);
CREATE INDEX idx_history_track ON listening_history (track_id);

CREATE TABLE likes (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    track_id   BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, track_id)
);

INSERT INTO listening_history (user_id, track_id, played_at) VALUES
    (1, 1, now() - interval '3 day'),
    (1, 4, now() - interval '2 day'),
    (1, 1, now() - interval '1 day'),
    (2, 5, now() - interval '5 hour'),
    (2, 4, now() - interval '2 hour'),
    (1, 7, now() - interval '1 hour');

INSERT INTO likes (user_id, track_id) VALUES
    (1, 1), (1, 5), (2, 6);
