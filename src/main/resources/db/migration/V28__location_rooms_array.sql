ALTER TABLE IF EXISTS public.locations
    ALTER room_code DROP default,
    ALTER room_code TYPE text[] USING ARRAY[room_code],
    ALTER room_code SET default '{}';
    RENAME room_code TO room_codes;