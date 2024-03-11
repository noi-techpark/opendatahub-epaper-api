CREATE TABLE display_content
(
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    image_url character varying(100),
    image_hash character varying(100),
    image_base64 text,
    display_id integer,
    template_id integer,
    scheduled_content_id integer,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN display_content_id integer;
ALTER TABLE IF EXISTS public.image_fields
    ADD CONSTRAINT fk_image_fields_display_content_id FOREIGN KEY (display_content_id)
    REFERENCES public.display_content (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_image_fields_display_content_id
    ON public.image_fields(display_content_id);
    

ALTER TABLE IF EXISTS public.display_content
    ADD CONSTRAINT fk_display_content_display_id FOREIGN KEY (display_id)
    REFERENCES public.displays (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_display_content_display_id
    ON public.display_content(display_id);


ALTER TABLE IF EXISTS public.display_content
    ADD CONSTRAINT fk_display_content_template_id FOREIGN KEY (template_id)
    REFERENCES public.templates (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_display_content_template_id
    ON public.display_content(template_id);
    

ALTER TABLE IF EXISTS public.display_content
    ADD CONSTRAINT fk_display_content_scheduled_content_id FOREIGN KEY (scheduled_content_id)
    REFERENCES public.scheduled_content (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_display_content_scheduled_content_id
    ON public.display_content(scheduled_content_id);