ALTER TABLE IF EXISTS public.displays
    ADD COLUMN image_base64 text,
    ADD COLUMN image_hash VARCHAR(100);

ALTER TABLE IF EXISTS public.templates
    ADD COLUMN multiple_room boolean,
    ADD COLUMN footer boolean,
    ADD COLUMN header boolean,
    ADD COLUMN room_data INTEGER[],
    ADD COLUMN resolution_id integer;

ALTER TABLE IF EXISTS public.templates
    ADD CONSTRAINT fk_resolution_id
    FOREIGN KEY (resolution_id) REFERENCES public.resolutions (id)
    ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.scheduled_content
    ADD COLUMN room VARCHAR(100),
    ADD COLUMN override boolean,
    ADD COLUMN image_base64 text;

ALTER TABLE IF EXISTS public.scheduled_content
    ADD COLUMN template_id integer;

ALTER TABLE IF EXISTS public.scheduled_content
    ADD CONSTRAINT fk_scheduled_content_template_id FOREIGN KEY (template_id)
    REFERENCES public.templates (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fki_fk_scheduled_content_template_id
    ON public.scheduled_content(template_id);

ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN repeat boolean,
    ADD COLUMN is_repeated boolean,
    ADD COLUMN italic boolean,
    ADD COLUMN bold boolean,
    ADD COLUMN border boolean,
    ADD COLUMN image text;

ALTER TABLE IF EXISTS public.display_content
    ADD COLUMN image_base64 text;

    

   

 

