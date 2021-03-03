#!/bin/bash
set -euo pipefail

ORIGINAL_POSTGRES_HOST="$1"
ORIGINAL_POSTGRES_PORT="$2"
ORIGINAL_POSTGRES_DB="$3"
ORIGINAL_POSTGRES_SCHEMA="$4"
ORIGINAL_POSTGRES_USERNAME="$5"
OUTPUT="$6"

TMP_FILE="orig-database-schemadump.sql"

echo "# Starting pg_dump of $ORIGINAL_POSTGRES_USERNAME@$ORIGINAL_POSTGRES_HOST/$ORIGINAL_POSTGRES_DB/$ORIGINAL_POSTGRES_SCHEMA"

echo "
--
-- Database Schema Dump of '$ORIGINAL_POSTGRES_HOST/$ORIGINAL_POSTGRES_DB/$ORIGINAL_POSTGRES_SCHEMA'
--
-- Please use the script infrastructure/utils/originaldb-dump-schema.sh to update this dump
--
" > "$TMP_FILE"

pg_dump \
    -v \
	--port="$ORIGINAL_POSTGRES_PORT" \
    --host="$ORIGINAL_POSTGRES_HOST" \
    --username="$ORIGINAL_POSTGRES_USERNAME" \
    --schema="$ORIGINAL_POSTGRES_SCHEMA" \
    --no-acl \
    --no-owner \
    --no-publications \
    --no-subscriptions \
    --no-comments \
    --schema-only \
    "$ORIGINAL_POSTGRES_DB" | \
    grep -v -e '^SET .*$' | \
    grep -v -E 'll_to_earth' | \
    sed -z -e "s#)\nWITH (autovacuum_analyze_threshold='2E9'##g" | \
    grep -v -E '^(CREATE\ EXTENSION|COMMENT\ ON\ EXTENSION|CREATE\ SCHEMA)' | \
    grep -v -e '^--.*$' | \
    grep -v -e '^[[:space:]]*$' >> "$TMP_FILE"

cat "$TMP_FILE" > "$OUTPUT"
rm "$TMP_FILE"
echo "# Updating $OUTPUT"
echo "> READY."
exit 0
