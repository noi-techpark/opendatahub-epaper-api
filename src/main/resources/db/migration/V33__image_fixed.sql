ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN fixed boolean;

UPDATE public.image_fields
    SET fixed = true;