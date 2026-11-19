-- ============================================================
-- V1 : schéma initial de LULU-app
-- Règle d'or : on ne modifie jamais une migration déjà appliquée.
-- Pour faire évoluer le schéma : V2__description.sql, V3__..., etc.
-- ============================================================

-- Profil de la candidate (une seule ligne). Aucune coordonnée personnelle :
-- ce sont ces informations qui sont envoyées à l'IA pour l'analyse des offres.
CREATE TABLE IF NOT EXISTS candidate_profile (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    full_name   VARCHAR(120),
    headline    VARCHAR(200),
    summary     VARCHAR(2000),
    experience  MEDIUMTEXT,
    skills      VARCHAR(1000),
    languages   VARCHAR(500),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Offres d'emploi (importées d'Adzuna ou ajoutées à la main) et suivi des candidatures
CREATE TABLE IF NOT EXISTS job_offer (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    source         VARCHAR(20)   NOT NULL,
    external_id    VARCHAR(100),
    title          VARCHAR(255)  NOT NULL,
    company        VARCHAR(255),
    location       VARCHAR(255),
    contract_type  VARCHAR(100),
    salary         VARCHAR(100),
    url            VARCHAR(1000),
    description    MEDIUMTEXT,
    published_at   DATETIME(6),

    -- Suivi de la candidature
    status         VARCHAR(20)   NOT NULL,
    applied_at     DATE,
    notes          VARCHAR(2000),

    -- Analyse IA
    ai_score       INT,
    ai_strengths   VARCHAR(2000),
    ai_gaps        VARCHAR(2000),
    ai_advice      VARCHAR(2000),
    cover_letter   MEDIUMTEXT,
    analysed_at    DATETIME(6),

    created_at     DATETIME(6),
    PRIMARY KEY (id),
    -- Une même offre Adzuna ne peut être importée qu'une fois
    CONSTRAINT uk_job_offer_source_external UNIQUE (source, external_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
