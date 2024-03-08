UPDATE image_fields SET x_pos = 0 WHERE x_POS IS NULL;
UPDATE image_fields SET y_pos = 0 WHERE y_pos IS NULL;
UPDATE image_fields SET font_size = 10 WHERE font_size IS NULL;
UPDATE image_fields SET height = 0 WHERE height IS NULL;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN x_pos SET DEFAULT 0;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN x_pos SET NOT NULL;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN y_pos SET DEFAULT 0;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN y_pos SET NOT NULL;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN font_size SET DEFAULT 10;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN font_size SET NOT NULL;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN height SET DEFAULT 0;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN height SET NOT NULL;