# Déploiement de LULU-app

LULU tourne sur le serveur Oracle **fle-presence** (Ampere A1, ARM), à côté de FLE Présence et PlanSécu,
derrière le **Caddy** de fle-presence qui fournit le HTTPS (certificat Let's Encrypt automatique).

```
Téléphone / PC ──HTTPS──▶ Caddy (fle-presence) ──réseau Docker──▶ lulu-app:8081 ──▶ lulu-db (MySQL)
                                                                     ▲
GitHub (push sur main) ──CI──▶ Docker Hub rolandmerhej1/lulu-app ────┘ (docker compose pull)
```

| Élément | Valeur |
|---|---|
| Adresse | https://lulu-app.duckdns.org |
| Dossier sur le serveur | `~/lulu-app` (clone du dépôt) |
| Conteneurs | `lulu-app` (512 Mo max), `lulu-db` (320 Mo max) |
| Port local de test | `127.0.0.1:8082` (8081 est pris par PlanSécu) |
| Caddyfile | `/home/ubuntu/fle-presence/deploiement/Caddyfile` |
| Sauvegardes | `~/lulu-sauvegardes`, chaque nuit à 3 h 40, 14 gardées |

## Première installation

1. **DuckDNS** : créer le sous-domaine `lulu-app` pointant vers `141.253.100.80`.
2. **GitHub** (dépôt lulu-app, *Settings > Secrets and variables > Actions*) : `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`.
   Après le merge sur main, la CI publie l'image.
3. **Serveur** :
   ```bash
   cd ~ && git clone https://github.com/Rmerhej/lulu-app.git && cd lulu-app
   cp .env.example .env && chmod 600 .env && nano .env    # vraies valeurs + PROXY_NETWORK=fle-presence_default
   docker compose -f docker-compose.prod.yml up -d
   bash deploy/connecter-proxy.sh
   ```
4. **Caddy** : ajouter le bloc suivant au Caddyfile, puis `caddy validate` et `caddy reload` :
   ```
   lulu-app.duckdns.org {
       encode zstd gzip
       reverse_proxy lulu-app:8081
   }
   ```
5. **Sauvegarde automatique** (`crontab -e`) :
   ```
   40 3 * * * /home/ubuntu/lulu-app/deploy/sauvegarde.sh >> /home/ubuntu/lulu-sauvegardes/cron.log 2>&1
   ```
6. **Surveillance** : sonde UptimeRobot (HTTPS) sur `https://lulu-app.duckdns.org/login`.

## Mise en ligne automatique

À chaque merge sur main, la CI enchaîne : tests, image Docker Hub, puis **Mise en ligne**
(connexion SSH au serveur et `deploy/mise-a-jour.sh`, qui vérifie que LULU répond).
Secrets du dépôt : `SERVEUR_HOTE` (141.253.100.80), `SERVEUR_UTILISATEUR` (ubuntu),
`SERVEUR_CLE_SSH` (clé privée `~/.ssh/fle_deploiement`, déjà autorisée sur le serveur).

## Au quotidien

| Besoin | Commande (sur le serveur) |
|---|---|
| Mettre à jour à la main (si besoin) | `bash ~/lulu-app/deploy/mise-a-jour.sh` |
| Journaux de l'appli | `docker logs --tail 80 lulu-app` |
| Mémoire utilisée | `docker stats --no-stream lulu-app lulu-db` |
| Sauvegarder maintenant | `bash ~/lulu-app/deploy/sauvegarde.sh` |
| Restaurer | `bash ~/lulu-app/deploy/restauration.sh <fichier>` |

## Sécurité en production

- HTTPS uniquement (Caddy) ; HSTS, CSP stricte, anti-iframe, Referrer-Policy envoyés par Spring Security.
- Cookies de session `HttpOnly`, `SameSite=Lax` et `Secure` (HTTPS détecté via `forward-headers-strategy`).
- Blocage de 15 min après 5 mots de passe faux depuis la même adresse.
- « Rester connectée » : jeton aléatoire en base (30 jours), supprimé à la déconnexion.
- Aucun port public : base et appli ne sont joignables que par Docker et Caddy.
- Secrets uniquement dans `~/lulu-app/.env` (droits 600), jamais dans Git ni dans l'image.

## Application installable (PWA)

- **iPhone** : ouvrir le site dans Safari > bouton Partager > *Sur l'écran d'accueil*.
- **Android** : Chrome propose « Installer LULU » (bannière en bas de l'écran).
- Le service worker (`sw.js`) met en cache la mise en page seulement. Les pages avec des données
  personnelles ne sont jamais stockées sur le téléphone ; sans réseau, une page « Pas de connexion » s'affiche.
