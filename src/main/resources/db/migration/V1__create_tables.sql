CREATE TABLE wac_core_players (
    uuid            BINARY(16) NOT NULL PRIMARY KEY,
    player_class    VARCHAR(32),
    UNIQUE (uuid)
);