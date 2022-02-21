CREATE TABLE giveaways
(
    id              IDENTITY NOT NULL PRIMARY KEY,
    guild_id        BIGINT          NOT NULL,
    channel_id      BIGINT          NOT NULL,
    message_id      BIGINT          DEFAULT NULL,
    hosted_by       BIGINT          NOT NULL,
    winners         BIGINT ARRAY    DEFAULT NULL,
    participants    BIGINT ARRAY    DEFAULT NULL,
    created_at      TIMESTAMP(0)    DEFAULT CURRENT_TIMESTAMP(0),
    due_at          TIMESTAMP(0)    NOT NULL,
    winner_prize   VARCHAR(64)    NOT NULL,
    winner_amount   INTEGER         NOT NULL,
    active          BOOL            NOT NULL DEFAULT TRUE
)
