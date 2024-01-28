CREATE TYPE role_type as ENUM ('ADMIN', 'USER');

CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    role       role_type,

    CONSTRAINT users_PK PRIMARY KEY (id)
);


INSERT INTO users (first_name, last_name, role)
VALUES ('FirsName1', 'LastName1', null);

INSERT INTO users (first_name, last_name, role)
VALUES ('FirsName2', 'LastName2', null);
