ALTER TABLE IF EXISTS public.templates
    ADD COLUMN max_rooms integer;

UPDATE public.templates
    SET max_rooms = 1
    WHERE max_rooms = NULL;