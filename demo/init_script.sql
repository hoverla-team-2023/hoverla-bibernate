CREATE TABLE IF NOT EXISTS music_record
(
    id     SERIAL PRIMARY KEY,
    title  VARCHAR(255),
    artist VARCHAR(255),
    genre  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS store_item
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    price   INT,
    version INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS item_comment
(
    id      SERIAL PRIMARY KEY,
    comment VARCHAR(255),
    item_id BIGINT,
    CONSTRAINT fk_store_item FOREIGN KEY (item_id) REFERENCES store_item (id)
);
