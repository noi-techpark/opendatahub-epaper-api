# SPDX-FileCopyrightText: 2026 NOI Techpark <digital@noi.bz.it>
#
# SPDX-License-Identifier: CC0-1.0
#

# Sync test DB from production (schema + data, including sequences)
set -e

# 1. Full dump from production (schema + data)
PGSSLMODE=require pg_dump -h $PROD_DB_HOST -U $DB_USERNAME -d epaper \
  -Fc -f /tmp/prod_data.dump

# 2. Restore into testing, dropping and recreating all objects cleanly
pg_restore -h $TEST_DB_HOST -U $DB_USERNAME -d epaper \
  --clean --if-exists \
  /tmp/prod_data.dump

# 3. Sync S3: clear test bucket and copy everything from production
export AWS_PROFILE=yourprofile
aws s3 rm s3://it.bz.opendatahub.epaper.images-test --recursive
aws s3 sync s3://it.bz.opendatahub.epaper.images-prod s3://it.bz.opendatahub.epaper.images-test
