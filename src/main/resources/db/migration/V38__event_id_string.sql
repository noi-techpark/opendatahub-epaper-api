ALTER TABLE scheduled_content ALTER COLUMN event_id TYPE VARCHAR(255) USING event_id::VARCHAR;
