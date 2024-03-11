ALTER TABLE IF EXISTS public.templates
    ADD COLUMN description character varying(200);
    
CREATE TABLE image_fields
(
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    x_pos integer,
    y_pos integer,
    custom_text character varying(200),
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);