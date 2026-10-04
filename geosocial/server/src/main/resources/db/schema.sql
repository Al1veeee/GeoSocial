-- Справочная схема БД (сервер создаёт таблицы автоматически при старте)

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    firebase_uid  VARCHAR(128) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    bio           VARCHAR(500),
    avatar_url    VARCHAR(500),
    created_at    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS places (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    category     VARCHAR(50) NOT NULL,
    latitude     DOUBLE PRECISION NOT NULL,
    longitude    DOUBLE PRECISION NOT NULL,
    address      VARCHAR(300),
    author_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS posts (
    id         BIGSERIAL PRIMARY KEY,
    place_id   BIGINT NOT NULL REFERENCES places (id) ON DELETE CASCADE,
    author_id  BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    text       VARCHAR(2000) NOT NULL,
    photo_url  VARCHAR(500),
    rating     INT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_places_coords ON places (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_posts_place ON posts (place_id);
