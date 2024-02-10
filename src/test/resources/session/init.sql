CREATE TYPE genre AS ENUM ('POP', 'RAP', 'LOFI');

CREATE TABLE IF NOT EXISTS music_record
(
    id     BIGSERIAL,
    title  VARCHAR(255),
    artist VARCHAR(255),
    genre  genre
);

INSERT INTO music_record (title, artist, genre)
VALUES ('Don''t Go Insane', 'DPR IAN', 'POP'),
       ('Self Care', 'Mac Miller', 'RAP'),
       ('mude', 'cliffe', 'LOFI');
