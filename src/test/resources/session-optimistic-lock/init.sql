CREATE TABLE IF NOT EXISTS entity_primitive_int_lock
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    version    INTEGER
);

INSERT INTO entity_primitive_int_lock (first_name, last_name, version)
VALUES ('Will', 'Smith', 1),
       ('Barak', 'Obama', 2);

CREATE TABLE IF NOT EXISTS entity_integer_lock
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    version    INTEGER
);

INSERT INTO entity_integer_lock (first_name, last_name, version)
VALUES ('Charly', 'Chaplin', 4),
       ('Gustav', 'Klimt', 2);

CREATE TABLE IF NOT EXISTS entity_primitive_long_lock
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    version    BIGINT
);

INSERT INTO entity_primitive_long_lock (first_name, last_name, version)
VALUES ('John', 'Doe', 1),
       ('Chuck', 'Palahniuk', 3);

CREATE TABLE IF NOT EXISTS entity_long_lock
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    version    BIGINT
);

INSERT INTO entity_long_lock (first_name, last_name, version)
VALUES ('Jack', 'London', 10),
       ('Benjamin', 'Franklin', 4);
