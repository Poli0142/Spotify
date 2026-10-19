CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ARTIST', 'ADMIN')),
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

INSERT INTO users (username, password, display_name, role) VALUES
    ('user',   crypt('user',   gen_salt('bf', 10)), 'Пользователь',  'USER'),
    ('artist', crypt('artist', gen_salt('bf', 10)), 'CODE80',        'ARTIST'),
    ('admin',  crypt('admin',  gen_salt('bf', 10)), 'Администратор', 'ADMIN');
