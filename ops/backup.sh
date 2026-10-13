#!/usr/bin/env bash
set -Eeuo pipefail
umask 077
release_dir="$(cd "$(dirname "$0")/.." && pwd)"
app_root="$(cd "$release_dir/../.." && pwd)"
exec 9>"$app_root/deploy.lock"
flock -w 600 9
compose() {
    sudo -n docker compose -p helpdesk --env-file "$app_root/.env" \
        --env-file "$release_dir/release.env" -f "$release_dir/compose.prod.yml" "$@"
}
mkdir -p "$app_root/backups"
temporary_dir="$(mktemp -d "$app_root/backups/.pending.XXXXXX")"
restart_backend=false
cleanup() {
    if $restart_backend; then compose start backend; fi
    rm -rf -- "$temporary_dir"
}
trap cleanup EXIT
# Prevent attachment writes while taking the database and file snapshots.
restart_backend=true
compose stop backend
compose exec -T postgres sh -c 'exec pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$temporary_dir/database.dump"
test -s "$temporary_dir/database.dump"
compose run --rm --no-deps -T --entrypoint tar backend czf - -C /app/uploads/attachments . > "$temporary_dir/attachments.tar.gz"
test -s "$temporary_dir/attachments.tar.gz"
compose start backend
restart_backend=false
backup_dir="$app_root/backups/helpdesk-$(date -u +%Y%m%dT%H%M%SZ)-$$"
mv "$temporary_dir" "$backup_dir"
echo "$backup_dir"
