ALTER TABLE IF EXISTS public.display_content DROP COLUMN IF EXISTS image_hash;

ALTER TABLE IF EXISTS public.displays
    ADD COLUMN image_hash character varying(100);