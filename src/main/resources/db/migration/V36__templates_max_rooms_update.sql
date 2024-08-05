UPDATE public.templates
    SET max_rooms = 1
    WHERE max_rooms IS NULL;