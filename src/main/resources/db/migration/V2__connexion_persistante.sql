-- "Rester connectée" : jetons de Spring Security (JdbcTokenRepositoryImpl).
-- Le cookie ne contient qu'un identifiant aléatoire, jamais le mot de passe.
CREATE TABLE persistent_logins (
    username  VARCHAR(64) NOT NULL,
    series    VARCHAR(64) PRIMARY KEY,
    token     VARCHAR(64) NOT NULL,
    last_used TIMESTAMP   NOT NULL
);
