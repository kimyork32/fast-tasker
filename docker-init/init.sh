#!/bin/bash
set -e

if [ -n "$DB_NAME_NOTIFICATION" ]; then
    echo "creating db: $DB_NAME_NOTIFICATION ---"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        CREATE DATABASE $DB_NAME_NOTIFICATION;
        GRANT ALL PRIVILEGES ON DATABASE $DB_NAME_NOTIFICATION TO $POSTGRES_USER;
EOSQL

    echo "db create successful"
fi