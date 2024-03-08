create TABLE scheduled_content (
    id SERIAL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    created TIMESTAMP NOT NULL,
    last_update TIMESTAMP NOT NULL,
    disabled boolean,
    event_id integer,
    start_date timestamp without time zone,
    end_date timestamp without time zone,
    room VARCHAR(100),
    override boolean,
    image_base64 text,
    
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.scheduled_content
    ADD COLUMN display_id integer;
ALTER TABLE IF EXISTS public.scheduled_content
    ADD CONSTRAINT fk_scheduled_content_display_id FOREIGN KEY (display_id)
    REFERENCES public.displays (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_scheduled_content_display_id
    ON public.scheduled_content(display_id);
    
ALTER TABLE IF EXISTS public.scheduled_content
    ADD COLUMN template_id integer;
ALTER TABLE IF EXISTS public.scheduled_content
    ADD CONSTRAINT fk_scheduled_content_template_id FOREIGN KEY (template_id)
    REFERENCES public.templates (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_scheduled_content_template_id
    ON public.scheduled_content(template_id);