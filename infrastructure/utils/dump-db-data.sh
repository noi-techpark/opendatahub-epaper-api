#!/bin/bash
set -euo pipefail

ORIGINAL_POSTGRES_HOST="$1"
ORIGINAL_POSTGRES_DB="$2"
ORIGINAL_POSTGRES_SCHEMA="$3"
ORIGINAL_POSTGRES_USERNAME="$4"
OUTPUT="$5"

TMP_FILE="orig-database-datadump.sql"

echo "# Starting pg_dump of $ORIGINAL_POSTGRES_USERNAME@$ORIGINAL_POSTGRES_HOST/$ORIGINAL_POSTGRES_DB/$ORIGINAL_POSTGRES_SCHEMA"

echo "
--
-- Database Schema Dump of '$ORIGINAL_POSTGRES_HOST/$ORIGINAL_POSTGRES_DB/$ORIGINAL_POSTGRES_SCHEMA'
--
-- Please use the script infrastructure/utils/dump-db-data.sh to update this dump
--
" > "$TMP_FILE"

pg_dump \
    -v \
    --host="$ORIGINAL_POSTGRES_HOST" \
    --username="$ORIGINAL_POSTGRES_USERNAME" \
    --schema="$ORIGINAL_POSTGRES_SCHEMA" \
    --no-acl \
    --no-owner \
    --no-publications \
    --no-subscriptions \
	--data-only \
	--column-inserts \
    "$ORIGINAL_POSTGRES_DB" >> "$TMP_FILE"

cat "$TMP_FILE" > "$OUTPUT"
rm "$TMP_FILE"
echo "# Updating $OUTPUT"
echo "> READY."
exit 0
