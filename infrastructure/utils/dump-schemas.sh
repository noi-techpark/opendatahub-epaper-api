#!/bin/bash

# ./originaldb-dump-schema.sh test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com 5432 epaper public pmoser dumptest.sql
./originaldb-dump-schema.sh localhost 5555 epaper public epaper dumplocal.sql

exit 0
