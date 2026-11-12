#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<-EOSQL
  CREATE ROLE replicador WITH REPLICATION LOGIN PASSWORD 'replica123';
  CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio NUMERIC(10,2) NOT NULL
  );
EOSQL
echo "host replication replicador all md5" >> "$PGDATA/pg_hba.conf"