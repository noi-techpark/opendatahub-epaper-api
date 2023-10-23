ALTER TABLE IF EXISTS public.scheduled_content
    ALTER room_codes DROP default,
    ALTER room_codes TYPE text[] USING array[room_codes],
    ALTER room_codes SET default '{}';

UPDATE public.display d
    set d.room_codes = l.room_codes
    from public.locations l
    where l.id = d.location_id;

ALTER TABLE  public.displays
DROP COLUMN IF EXISTS location_id;

DROP TABLE IF EXISTS public.display_locations;
DROP TABLE IF EXISTS public.locations;