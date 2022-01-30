ALTER TABLE IF EXISTS public.displays DROP COLUMN IF EXISTS image_hash;

ALTER TABLE IF EXISTS public.display_content
    ADD COLUMN image_hash character varying(100);