#!/usr/bin/env bash
# Met LULU à jour avec la dernière image publiée par la CI, puis la rebranche sur Caddy.
# Usage (sur le serveur) : bash ~/lulu-app/deploy/mise-a-jour.sh
set -euo pipefail
PROJET="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJET"

echo "1/4 Récupération des scripts à jour (git pull)"
git pull --ff-only

echo "2/4 Téléchargement de la dernière image"
docker compose -f docker-compose.prod.yml pull

echo "3/4 Redémarrage (la base de données et ses données sont conservées)"
docker compose -f docker-compose.prod.yml up -d
bash "$PROJET/deploy/connecter-proxy.sh"

echo "4/4 Vérification (jusqu'à 2 minutes)"
for i in $(seq 1 24); do
  if curl -fsS -o /dev/null http://127.0.0.1:8082/login; then
    echo "LULU répond. Mise à jour terminée."
    docker image prune -f > /dev/null
    exit 0
  fi
  sleep 5
done
echo "LULU ne répond pas après 2 minutes. Derniers journaux :"
docker logs --tail 80 lulu-app
exit 1
