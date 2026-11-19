# LULU

[![CI](https://github.com/Rmerhej/LULU-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Rmerhej/LULU-app/actions/workflows/ci.yml)

Carnet de candidatures pour une recherche d'emploi dans le **luxe et la mode**, en Île-de-France :
développement produit et matières, chef de produit, achat, production.

## Ce que fait l'application

- **Rechercher des offres** sur les sites d'emploi via l'API Adzuna, avec des expressions prêtes à l'emploi
  (français et anglais), et les ajouter en un clic sans doublon.
- **Ajouter une offre vue ailleurs** (site carrière d'une maison, LinkedIn, Welcome to the Jungle) en collant
  son texte : l'IA en retire l'intitulé, la maison, le lieu et le contrat.
- **Analyser une offre** : l'IA la compare au profil, donne une note d'adéquation, les points forts, ce qui
  manque, des conseils, et rédige une lettre de motivation dans la langue de l'annonce.
- **Suivre chaque candidature** : à étudier, à postuler, envoyée, relancée, entretien, offre, refus.
- **Accueil** : chiffres clés et taux d'entretiens obtenus.

## Stack technique

| Couche | Outils |
|---|---|
| Back-end | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation |
| Front-end | Thymeleaf, Bootstrap 5, CSS sur mesure (Bodoni Moda, Work Sans) |
| Données | MySQL 8, migrations Flyway · H2 pour les tests |
| Offres | API Adzuna via `RestClient` |
| IA | Spring AI 2.0 + Google Gemini |
| Sécurité | Spring Security (connexion, BCrypt, CSRF) |
| Déploiement | Docker (image multi-étapes), Docker Compose |
| Intégration continue | GitHub Actions : tests + construction de l'image |

## Lancer le projet

```bash
cp .env.example .env      # puis remplir les valeurs (voir ci-dessous)
docker compose up -d --build
```

Ouvrir http://localhost:8081

Clés à obtenir (gratuites) :
- **Adzuna** : https://developer.adzuna.com → `ADZUNA_APP_ID`, `ADZUNA_APP_KEY`
- **Gemini** : https://aistudio.google.com/apikey → `GEMINI_API_KEY`

Pour développer dans IntelliJ : `docker compose up -d lulu-db`, puis lancer l'application (port 8081).

## Tests

```bash
./mvnw test
```

Aucun appel réel à Adzuna ni à l'IA : les API sont simulées, la base est H2 en mémoire.

## Données personnelles

- Le profil ne contient aucune coordonnée (téléphone, e-mail, adresse).
- Le nom sert uniquement à signer les lettres : il n'est jamais envoyé à l'IA.
- Avec l'offre gratuite de Gemini, les textes envoyés peuvent servir à améliorer les modèles de Google :
  n'ajoutez au profil que ce qui figure déjà sur le CV.
- Les secrets restent dans `.env`, jamais dans Git ni dans l'image Docker.

## Auteur

Roland Merhej — [LinkedIn](https://www.linkedin.com/in/rolandmerhej)
