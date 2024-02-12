CREATE TABLE IF NOT EXISTS test_entity
(
    id         BIGSERIAL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email       VARCHAR(255),
    comment_id  BIGINT,


    CONSTRAINT users_PK PRIMARY KEY (id)
    );


INSERT INTO test_entity (first_name, last_name, email, comment_id)
VALUES ('FirsName1', 'LastName1', 'test@email.com', 1);

INSERT INTO test_entity (first_name, last_name, email)
VALUES ('FirsName2', 'LastName2', 'test2@email.com');


DROP TABLE IF EXISTS users;
