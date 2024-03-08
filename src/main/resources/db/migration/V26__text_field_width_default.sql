
UPDATE image_fields SET width = 0 WHERE width IS NULL;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN width SET DEFAULT 0;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN width SET NOT NULL;