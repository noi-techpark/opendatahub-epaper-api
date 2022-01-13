ALTER TABLE IF EXISTS public.templates
    ADD COLUMN description character varying(200);
    
CREATE TABLE image_fields
(
    id integer NOT NULL,
    uuid character varying(36) NOT NULL,
    x_pos integer,
    y_pos integer,
    custom_text character varying(200),
    created timestamp without time zone NOT NULL,
    last_update timestamp without time zone NOT NULL,
    PRIMARY KEY (id)
);