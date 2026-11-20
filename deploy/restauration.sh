#!/usr/bin/env bash
# Restaure une sauvegarde de la base LULU (REMPLACE les données actuelles).
# Usage : bash deploy/restauration.sh ~/lulu-sauvegardes/lulu-AAAA-MM-JJ_HHMM.sql.gz
set -euo pipefail
PROJET="$(cd "$(dirname "$0")/.." && pwd)"
FICHIER="${1:-}"
if [ -z "$FICHIER" ] || [ ! -f "$FICHIER" ]; then
  echo "Indiquez un fichier de sauvegarde. Disponibles :"
  ls -1t "${LULU_BACKUP_DIR:-$HOME/lulu-sauvegardes}"/lulu-*.sql.gz 2>/dev/null | head -14
  exit 1
fi
read -r -p "Les données actuelles de LULU seront remplacées par $FICHIER. Taper OUI pour confirmer : " REPONSE
[ "$REPONSE" = "OUI" ] || { echo "Annulé."; exit 1; }

cd "$PROJET"
echo "Sauvegarde de sécurité de l'état actuel…"
bash "$PROJET/deploy/sauvegarde.sh"
docker compose -f docker-compose.prod.yml stop lulu-app
gunzip -c "$FICHIER" | docker exec -i lulu-db sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" lulu_app'
docker compose -f docker-compose.prod.yml start lulu-app
bash "$PROJET/deploy/connecter-proxy.sh"
echo "Restauration terminée."
