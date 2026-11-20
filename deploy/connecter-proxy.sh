#!/usr/bin/env bash
# Branche le conteneur lulu-app sur le réseau Docker du Caddy de fle-presence (PROXY_NETWORK dans .env).
# On utilise "docker network connect" : seul le nom "lulu-app" est publié sur ce réseau,
# pas un nom court comme "app" qui entrerait en conflit avec fle-presence.
set -euo pipefail
PROJET="$(cd "$(dirname "$0")/.." && pwd)"
RESEAU="$(grep -E '^PROXY_NETWORK=' "$PROJET/.env" 2>/dev/null | cut -d= -f2- | tr -d '"' | tr -d "'" || true)"
if [ -z "$RESEAU" ]; then
  echo "PROXY_NETWORK absent de .env : rien à brancher."
  exit 0
fi
if docker inspect lulu-app --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}} {{end}}' | grep -qw "$RESEAU"; then
  echo "lulu-app est déjà sur le réseau $RESEAU"
else
  docker network connect "$RESEAU" lulu-app
  echo "lulu-app branché sur le réseau $RESEAU"
fi
