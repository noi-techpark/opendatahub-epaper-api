ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN bold boolean;
UPDATE public.image_fields
    SET bold = false
    WHERE bold IS NULL;

ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN italic boolean;
UPDATE public.image_fields
    SET italic = false
    WHERE italic IS NULL;