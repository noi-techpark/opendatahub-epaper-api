UPDATE public.displays
    SET room_codes = array[]::text[]
    WHERE room_codes IS NULL or (SELECT unnest(room_codes) IS NULL);