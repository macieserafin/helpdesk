#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-./backups}"
STAMP="$(date +%Y%m%d-%H%M%S)"

mkdir -p "$BACKUP_DIR"
case "$BACKUP_DIR" in
  /*) BACKUP_MOUNT="$BACKUP_DIR" ;;
  *) BACKUP_MOUNT="$PWD/$BACKUP_DIR" ;;
esac

docker compose -f docker-compose.prod.yml exec -T postgres sh -c \
  'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' \
  | gzip > "$BACKUP_DIR/helpdesk_db_$STAMP.sql.gz"

docker run --rm \
  -v helpdesk_prod_backend_uploads:/data:ro \
  -v "$BACKUP_MOUNT:/backup" \
  busybox tar czf "/backup/helpdesk_uploads_$STAMP.tar.gz" -C /data .

echo "Created:"
echo "$BACKUP_DIR/helpdesk_db_$STAMP.sql.gz"
echo "$BACKUP_DIR/helpdesk_uploads_$STAMP.tar.gz"
