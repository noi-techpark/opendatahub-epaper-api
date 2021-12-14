ALTER TABLE IF EXISTS public.displays
    ADD COLUMN location_id integer;
ALTER TABLE IF EXISTS public.displays
    ADD CONSTRAINT fk_displays_locations_id FOREIGN KEY (location_id)
    REFERENCES public.locations (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS fki_fk_displays_locations_id
    ON public.displays(location_id);