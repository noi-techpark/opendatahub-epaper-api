create TABLE resolutions (
	id SERIAL,
	uuid VARCHAR(36) NOT NULL UNIQUE,
	width INT,
	height INT,
	UNIQUE (width, height),
	PRIMARY KEY (id)
);

create TABLE displays (
    id SERIAL,
    mac VARCHAR(17) UNIQUE,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    network_address VARCHAR(25) UNIQUE,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    last_real_display_update TIMESTAMP,
    last_state TIMESTAMP NOT NULL,
    resolution_id SERIAL NOT NULL,
    image BYTEA,
    battery_percentage INT,
    PRIMARY KEY (id),
    FOREIGN KEY (resolution_id) REFERENCES resolutions (id) ON DELETE CASCADE
);

create TABLE locations (
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);


--CREATE TYPE protocol_type AS ENUM ('WLAN', 'LORAWAN');

create TABLE connections (
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    network_address VARCHAR(25) NOT NULL,
--    protocol protocol_type NOT NULL,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    coordinates bytea NOT NULL,
    display_id SERIAL NOT NULL,
    location_id SERIAL NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (display_id) REFERENCES displays (id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
);

create TABLE templates (
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created  TIMESTAMP NOT NULL,
    last_update  TIMESTAMP NOT NULL,
    image BYTEA NOT NULL,
    PRIMARY KEY (id)
);
