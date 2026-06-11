# SPDX-FileCopyrightText: 2026 NOI Techpark <digital@noi.bz.it>
#
# SPDX-License-Identifier: CC0-1.0
# 

# Update test with the production config
set -e

# 1. Dump data only from production
pg_dump -h $PROD_DB_HOST -U $DB_USERNAME -d epaper \
  --data-only \
  --disable-triggers \
  -Fc -f /tmp/prod_data.dump

# 2. Truncate all tables in testing (data only, keeps schema)
psql -h $TEST_DB_HOST -U $DB_USERNAME -d epaper -c "
DO \$\$ DECLARE r RECORD;
BEGIN
  FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
    EXECUTE 'TRUNCATE TABLE public.' || quote_ident(r.tablename) || ' CASCADE';
  END LOOP;
END \$\$;"

# 3. Restore data into testing
pg_restore -h $TEST_DB_HOST -U $DB_USERNAME -d epaper \
  --data-only \
  --disable-triggers \
  /tmp/prod_data.dump