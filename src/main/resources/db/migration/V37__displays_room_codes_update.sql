UPDATE public.displays
    SET room_codes = array[]::text[]
    WHERE room_codes IS NULL OR array_position(room_codes, NULL) IS NOT NULL;