CREATE TABLE image_fields
(
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    x_pos integer,
    y_pos integer,
    repeat boolean,
    is_repeated boolean, 
    italic boolean, 
    bold boolean, 
    border boolean,
    image text,    
    custom_text character varying(200),
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);