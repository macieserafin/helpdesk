#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

# A release contains compose.prod.yml and ops/deploy.sh.
release_dir="$(cd "$(dirname "$0")/.." && pwd)"
app_root="$(cd "$release_dir/../.." && pwd)"
image_tag="${1:?Pass the commit SHA}"
registry_user="${2:?Pass the GitHub actor}"
export IMAGE_NAMESPACE="${3:?Pass the GHCR namespace}"
[[ "$IMAGE_NAMESPACE" =~ ^[a-z0-9][a-z0-9_-]*$ ]] || { echo "Invalid namespace" >&2; exit 1; }
[[ "$image_tag" =~ ^[0-9a-f]{40}$ ]] || { echo "Invalid commit SHA" >&2; exit 1; }
[[ -f "$app_root/.env" ]] || { echo "Missing $app_root/.env" >&2; exit 1; }
if grep -q 'REPLACE_WITH_RANDOM_HEX_PASSWORD' "$app_root/.env"; then
    echo "Set a real database password first" >&2
    exit 1
fi
app_port="${4:-3001}"
[[ "$app_port" =~ ^[0-9]{1,5}$ ]] && (( 10#$app_port >= 1024 && 10#$app_port <= 65535 )) || exit 1
sudo -n true
exec 9>"$app_root/deploy.lock"
flock -w 600 9
printf 'APP_PORT=%s\nIMAGE_TAG=%s\nIMAGE_NAMESPACE=%s\n' "$app_port" "$image_tag" "$IMAGE_NAMESPACE" > "$release_dir/release.env"

auth_dir="$(mktemp -d "$app_root/docker-auth.XXXXXX")"
trap 'rm -rf -- "$auth_dir"' EXIT
export IMAGE_TAG="$image_tag"

compose() {
    sudo -n --preserve-env=IMAGE_TAG,IMAGE_NAMESPACE docker --config "$auth_dir" compose \
        --project-name helpdesk --env-file "$app_root/.env" --env-file "$release_dir/release.env" \
        -f "$release_dir/compose.prod.yml" "$@"
}

compose config --quiet
printf '%s\n' "$IMAGE_NAMESPACE" > "$release_dir/image-namespace"
# Create credentials as the directory owner. A sudo-run Docker Snap may be
# unable to write into another user's home even when Unix permissions allow it.
# The short-lived GITHUB_TOKEN is supplied on stdin, never as a CLI argument.
registry_auth="$( { printf '%s:' "$registry_user"; cat; } | base64 | tr -d '\r\n')"
printf '{"auths":{"ghcr.io":{"auth":"%s"}}}\n' "$registry_auth" > "$auth_dir/config.json"
unset registry_auth
compose pull

previous_release=""
if [[ -L "$app_root/current" ]]; then
    previous_release="$(readlink -f "$app_root/current")"
fi

if compose up -d --pull never --wait --wait-timeout 240 && \
    curl -fsS --max-time 15 http://127.0.0.1:$app_port/ >/dev/null && \
    curl -fsS --max-time 15 http://127.0.0.1:$app_port/api/health >/dev/null; then
    ln -sfn "$release_dir" "$app_root/current"
    echo "Deployed $image_tag"
else
    echo "Deployment failed" >&2
    compose ps
    if [[ -n "$previous_release" && "$previous_release" != "$release_dir" ]]; then
        export IMAGE_TAG="$(basename "$previous_release")"
        export IMAGE_NAMESPACE="$(cat "$previous_release/image-namespace")"
        echo "Restoring $IMAGE_TAG" >&2
        sudo -n --preserve-env=IMAGE_TAG,IMAGE_NAMESPACE docker compose \
            --project-name helpdesk --env-file "$app_root/.env" --env-file "$previous_release/release.env" \
            -f "$previous_release/compose.prod.yml" \
            up -d --pull never --wait --wait-timeout 240
    else
        echo "No previous release available; inspect container logs" >&2
    fi
    exit 1
fi
