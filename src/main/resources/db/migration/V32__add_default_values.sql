ALTER TABLE IF EXISTS public.templates
    ALTER COLUMN multiple_room SET DEFAULT false,
    ALTER COLUMN footer SET DEFAULT false,
    ALTER COLUMN header SET DEFAULT false;

    
ALTER TABLE IF EXISTS public.scheduled_content ALTER COLUMN override SET DEFAULT false;

ALTER TABLE IF EXISTS public.image_fields
    ALTER COLUMN repeat SET DEFAULT false,
    ALTER COLUMN is_repeated SET DEFAULT false,
    ALTER COLUMN italic SET DEFAULT false,
    ALTER COLUMN bold SET DEFAULT false,
    ALTER COLUMN border SET DEFAULT false;