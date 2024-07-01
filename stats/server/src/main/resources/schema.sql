DROP TABLE IF EXISTS endpoint_hits;
CREATE TABLE IF NOT EXISTS endpoint_hits
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app varchar(100) NOT NULL,
    uri  varchar(100) NOT NULL,
    ip varchar(100) NOT NULl,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);