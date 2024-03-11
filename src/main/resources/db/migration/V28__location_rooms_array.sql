ALTER TABLE IF EXISTS public.locations
    ALTER room_code DROP default,
    ALTER room_code TYPE text[] USING array[room_code],
    ALTER room_code SET default '{}';

ALTER TABLE IF EXISTS public.locations
    RENAME COLUMN room_code TO room_codes;