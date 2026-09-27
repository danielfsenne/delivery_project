#!/bin/bash
# Creates one database per microservice (database-per-service pattern).
set -e

for db in auth_db restaurant_db order_db payment_db delivery_db; do
  echo "Creating database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE $db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done
