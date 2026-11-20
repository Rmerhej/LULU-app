#!/usr/bin/env bash
# Sauvegarde de la base LULU (export SQL compressé), à lancer chaque nuit par cron.
# Les 14 dernières sauvegardes sont gardées dans ~/lulu-sauvegardes.
set -euo pipefail
PROJET="$(cd "$(dirname "$0")/.." && pwd)"
DOSSIER="${LULU_BACKUP_DIR:-$HOME/lulu-sauvegardes}"
GARDER=14
mkdir -p "$DOSSIER"
chmod 700 "$DOSSIER"

FICHIER="$DOSSIER/lulu-$(date +%Y-%m-%d_%H%M).sql.gz"
# Le mot de passe est lu dans le conteneur (variable MYSQL_ROOT_PASSWORD) : il n'apparaît
# ni dans la ligne de commande ni dans l'historique.
docker exec lulu-db sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --no-tablespaces lulu_app' \
  | gzip > "$FICHIER.tmp"
mv "$FICHIER.tmp" "$FICHIER"
chmod 600 "$FICHIER"

# Rotation : on supprime les plus anciennes
ls -1t "$DOSSIER"/lulu-*.sql.gz | tail -n +$((GARDER + 1)) | xargs -r rm -f
echo "Sauvegarde : $FICHIER ($(du -h "$FICHIER" | cut -f1))"
