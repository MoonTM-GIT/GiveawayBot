CREATE TABLE giveaways
(
    id              BIGINT           AUTO_INCREMENT,
    guild_id        BIGINT          NOT NULL,
    channel_id      BIGINT          NOT NULL,
    message_id      BIGINT          DEFAULT NULL,
    hosted_by       BIGINT          NOT NULL,
    winners         BIGINT ARRAY    DEFAULT NULL,
    participants    BIGINT ARRAY    DEFAULT NULL,
    created_at      TIMESTAMP(0)    DEFAULT CURRENT_TIMESTAMP(0),
    due_at          TIMESTAMP(0)    NOT NULL,
    winner_reward   VARCHAR(256)    NOT NULL,
    winner_amount   INTEGER         NOT NULL,
    active          BOOL            NOT NULL DEFAULT TRUE
)
