ALTER TABLE IF EXISTS public.displays
    ALTER COLUMN resolution_id DROP DEFAULT;

ALTER TABLE IF EXISTS public.displays
    ALTER COLUMN resolution_id DROP NOT NULL;
    
ALTER TABLE IF EXISTS public.displays DROP CONSTRAINT IF EXISTS displays_resolution_id_fkey;

ALTER TABLE IF EXISTS public.displays
    ADD CONSTRAINT displays_resolution_id_fkey FOREIGN KEY (resolution_id)
    REFERENCES public.resolutions (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

UPDATE public.displays set resolution_id = NULL;

DELETE FROM public.resolutions;

ALTER TABLE IF EXISTS public.resolutions DROP CONSTRAINT IF EXISTS resolutions_width_height_key;

ALTER TABLE IF EXISTS public.resolutions DROP CONSTRAINT IF EXISTS uk3088wuap16b1b4ctjn3s2a7ne;

ALTER TABLE IF EXISTS public.resolutions DROP CONSTRAINT IF EXISTS uk_resolution_width_height;

ALTER TABLE IF EXISTS public.resolutions
    ADD COLUMN bit_depth integer;
ALTER TABLE IF EXISTS public.resolutions
    ADD CONSTRAINT resolutions_resolutions_width_height_key_bitdepth UNIQUE (width, height, bit_depth);

