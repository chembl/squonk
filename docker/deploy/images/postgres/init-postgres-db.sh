#!/bin/bash

PG_USER=${POSTGRES_USER:-postgres}
echo "Setting up db as user ${PG_USER}"

psql --username "$PG_USER" --command "CREATE USER keycloak WITH PASSWORD '${POSTGRES_KEYCLOAK_PASSWORD:-squonk}';"
echo "created keycloak user with password ${POSTGRES_KEYCLOAK_PASSWORD:-squonk}"
psql --username "$PG_USER" --command "CREATE USER xwiki WITH PASSWORD '${POSTGRES_XWIKI_PASSWORD:-squonk}';"
echo "created xwiki user with password ${POSTGRES_XWIKI_PASSWORD:-squonk}"
psql --username "$PG_USER" --command "CREATE USER squonk WITH PASSWORD '${POSTGRES_SQUONK_PASSWORD:-squonk}';"
echo "created squonk user with password ${POSTGRES_SQUONK_PASSWORD:-squonk}"

createdb --username "$PG_USER" -O $PG_USER squonk
createdb --username "$PG_USER" -O keycloak keycloak
createdb --username "$PG_USER" -O xwiki xwiki


psql --username "$PG_USER" --command "GRANT CONNECT ON DATABASE squonk TO squonk;"
psql --username "$PG_USER" -d squonk --command "CREATE SCHEMA users AUTHORIZATION ${PG_USER};"
psql --username "$PG_USER" -d squonk --command "GRANT USAGE ON SCHEMA users TO squonk;"
psql --username "$PG_USER" -d squonk --command "CREATE SCHEMA notebooks AUTHORIZATION squonk;"


# patch 1. Create chemcentral database
# docker exec -it -u postgres deploy_postgres_1 bash
createdb --username "$PG_USER" -O $PG_USER chemcentral
psql --username "$PG_USER" --command 'create extension rdkit' chemcentral
psql --username "$PG_USER" -d chemcentral --command "CREATE SCHEMA vendordbs AUTHORIZATION squonk;"
