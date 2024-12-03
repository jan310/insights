CREATE TABLE IF NOT EXISTS users
(
    id                          VARCHAR(64)     PRIMARY KEY,
    email                       VARCHAR(320)    UNIQUE NOT NULL,
    notification_enabled        BOOLEAN         NOT NULL,
    notification_filter_tags    VARCHAR(100)[]  NOT NULL
);

CREATE TABLE IF NOT EXISTS sources
(
    id                          BIGSERIAL       PRIMARY KEY,
    user_id                     VARCHAR(64)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                        VARCHAR(100)    NOT NULL,
    description                 VARCHAR(300),
    isbn_13                     CHAR(13)
);
CREATE INDEX IF NOT EXISTS idx_sources_user_id ON sources(user_id);

CREATE TABLE IF NOT EXISTS insights
(
    id                          BIGSERIAL       PRIMARY KEY,
    user_id                     VARCHAR(64)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_id                   BIGINT          REFERENCES sources(id) ON DELETE CASCADE,
    last_modified_date          DATE            NOT NULL,
    filter_tags                 VARCHAR(100)[]  NOT NULL,
    note                        VARCHAR(1000)   NOT NULL,
    quote                       VARCHAR(1000)
);
CREATE INDEX IF NOT EXISTS idx_insights_user_id ON insights(user_id);
CREATE INDEX IF NOT EXISTS idx_insights_source_id ON insights(source_id);
CREATE INDEX IF NOT EXISTS idx_insights_filter_tags ON insights USING GIN(filter_tags);
