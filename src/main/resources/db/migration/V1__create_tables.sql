create TABLE displays (
    id BIGINT,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    network_address VARCHAR(25) UNIQUE,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    last_state TIMESTAMP NOT NULL,
    image BYTEA,
    battery_percentage INT,
    PRIMARY KEY (id)
);

create TABLE locations (
    id BIGINT,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);


CREATE TYPE protocol_type AS ENUM ('WLAN', 'LORAWAN');

create TABLE connections (
    id BIGINT,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE,
    network_address VARCHAR(25) NOT NULL,
    protocol protocol_type NOT NULL,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    coordinates VARCHAR(50) NOT NULL,
    display_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (display_id) REFERENCES displays (id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
);

create TABLE templates (
    id BIGINT,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created  TIMESTAMP NOT NULL,
    last_update  TIMESTAMP NOT NULL,
    image BYTEA NOT NULL,
    PRIMARY KEY (id)
);
