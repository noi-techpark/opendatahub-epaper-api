ALTER TABLE IF EXISTS public.templates
    ADD COLUMN resolution_id integer;

ALTER TABLE IF EXISTS public.templates
    ADD FOREIGN KEY (resolution_id) REFERENCES resolutions (id) ON DELETE CASCADE;

UPDATE public.templates
    SET resolution_id = r.id
    FROM public.resolutions r
    WHERE r.width = 1440 AND r.height = 2560;

ALTER TABLE IF EXISTS public.templates
    ALTER COLUMN resolution_id SET NOT NULL;