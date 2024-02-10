CREATE TABLE IF NOT EXISTS test_entity
(
    id         BIGSERIAL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email       VARCHAR(255),

    CONSTRAINT users_PK PRIMARY KEY (id)
    );


INSERT INTO test_entity (first_name, last_name, email)
VALUES ('FirsName1', 'LastName1', 'test@email.com');

INSERT INTO test_entity (first_name, last_name, email)
VALUES ('FirsName2', 'LastName2', 'test2@email.com');


DROP TABLE IF EXISTS users;
