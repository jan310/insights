CREATE TYPE FILTERTAG AS ENUM
(
    'PERSONAL_DEVELOPMENT',
    'WEALTH_CREATION'
);
CREATE CAST (VARCHAR AS FILTERTAG) WITH INOUT AS IMPLICIT;

CREATE TABLE users
(
    id                          VARCHAR(64)     PRIMARY KEY,
    email                       VARCHAR(320)    UNIQUE NOT NULL,
    notification_enabled        BOOLEAN         NOT NULL,
    notification_filter_tags    FILTERTAG[]     NOT NULL
);

CREATE TABLE sources
(
    id                          BIGSERIAL       PRIMARY KEY,
    user_id                     VARCHAR(64)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                        VARCHAR(100)    NOT NULL,
    description                 VARCHAR(300),
    isbn13                      CHAR(13)
);
CREATE INDEX ON sources(user_id);

CREATE TABLE insights
(
    id                          BIGSERIAL       PRIMARY KEY,
    source_id                   BIGINT          REFERENCES sources(id) ON DELETE CASCADE,
    last_modified_date          DATE            NOT NULL,
    filter_tags                 FILTERTAG[]     NOT NULL,
    note                        VARCHAR(1000),
    quote                       VARCHAR(1000),
    CHECK (note IS NOT NULL OR quote IS NOT NULL)
);
CREATE INDEX ON insights(source_id);
CREATE INDEX ON insights USING GIN(filter_tags);

INSERT INTO users (id, email, notification_enabled, notification_filter_tags)
VALUES ('AsYz88yBOFC2LCbuym93br1V0rYf9jic@clients', 'jan310.ondra@gmail.com', true, ARRAY[]::FILTERTAG[]);
