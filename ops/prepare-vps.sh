#!/usr/bin/env bash
set -Eeuo pipefail
umask 077
domain="${1:?Pass the application domain}"
[[ "$domain" =~ ^[a-zA-Z0-9.-]+$ ]] || exit 1
app_root="$HOME/apps/helpdesk"
mkdir -p "$app_root/releases"
chmod 700 "$app_root"
if [[ -e "$app_root/.env" ]]; then
    echo 'Existing environment preserved; inspect before changing.'
    exit 1
fi
admin_password="$(openssl rand -hex 32)"
db_password="$(openssl rand -hex 32)"
set -o noclobber
printf 'POSTGRES_DB=helpdesk\nPOSTGRES_USER=helpdesk\nPOSTGRES_PASSWORD=%s\nAPP_CORS_ALLOWED_ORIGIN_PATTERNS=https://%s\nAPP_DEMO_SEED_ENABLED=true\nAPP_DEMO_SEED_ACCOUNTS_ENABLED=true\nAPP_DEMO_SEED_CATEGORIES_ENABLED=true\nAPP_DEMO_SEED_TICKETS_ENABLED=false\nAPP_DEMO_SEED_ADMIN_PASSWORD=%s\n' "$db_password" "$domain" "$admin_password" > "$app_root/.env"
printf 'Login: admin\nPassword: %s\n' "$admin_password" > "$app_root/admin-credentials.txt"
unset admin_password db_password
chmod 600 "$app_root/.env" "$app_root/admin-credentials.txt"
echo 'Environment and private admin credential file created.'
