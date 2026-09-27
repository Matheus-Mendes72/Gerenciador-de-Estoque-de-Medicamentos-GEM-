#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

BACKUP_DIR="$PROJECT_DIR/backups/prod"

CONTAINER="gem-postgres-prod"
DATABASE="estoque_hospital"
USER="estoque_app"

DATE=$(date +"%Y-%m-%d_%H-%M-%S")

BACKUP_FILE="$BACKUP_DIR/estoque_hospital_$DATE.sql"

mkdir -p "$BACKUP_DIR"

docker exec "$CONTAINER" \
    pg_dump \
    -U "$USER" \
    -d "$DATABASE" \
    > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Backup criado: $BACKUP_FILE"
else
    echo "ERRO ao criar backup"
    rm -f "$BACKUP_FILE"
    exit 1
fi