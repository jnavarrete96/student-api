CREATE TABLE IF NOT EXISTS student (
    id        VARCHAR(36)  NOT NULL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status    VARCHAR(10)  NOT NULL CHECK (status IN ('ACTIVE','INACTIVE')),
    age       INT          NOT NULL CHECK (age >= 0)
);