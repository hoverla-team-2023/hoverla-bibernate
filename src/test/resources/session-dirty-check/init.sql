CREATE TYPE genre AS ENUM ('POP', 'RAP', 'LOFI');

CREATE TABLE IF NOT EXISTS music_record
(
    id     BIGSERIAL,
    title  VARCHAR(255),
    artist VARCHAR(255),
    genre  genre,

    CONSTRAINT music_record_PK PRIMARY KEY (id)
);

INSERT INTO music_record (title, artist, genre)
VALUES ('Don''t Go Insane', 'DPR IAN', 'POP'),
       ('Self Care', 'Mac Miller', 'RAP'),
       ('mude', 'cliffe', 'LOFI');

CREATE TABLE IF NOT EXISTS store_item
(
    id    BIGSERIAL,
    name  VARCHAR(255),
    price INTEGER,

    CONSTRAINT store_item_PK PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS item_comment
(
    id      BIGSERIAL,
    comment VARCHAR(255),
    item_id BIGINT,

    CONSTRAINT item_comment_PK PRIMARY KEY (id),
    CONSTRAINT item_comment_FK FOREIGN KEY (item_id) REFERENCES store_item (id)
);

INSERT INTO store_item (name, price)
VALUES ('Playstation 5', null),
       ('iPhone 13', 1000),
       ('Samsung S23', 1000);

INSERT INTO item_comment (comment, item_id)
VALUES ('The best thing I ever bought!', 1),
       ('Do not recommend. Buy Samsung', 2),
       ('The best smartphone', 2),
       ('I love this phone', 3),
       ('Do not recommend', null);
