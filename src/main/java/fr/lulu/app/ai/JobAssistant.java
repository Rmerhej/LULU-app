package fr.lulu.app.ai;

import fr.lulu.app.entity.CandidateProfile;
import fr.lulu.app.entity.JobOffer;

/**
 * L'assistant IA de LULU-app. Le reste de l'application ne dépend que de cette interface :
 * changer de fournisseur d'IA ne demande qu'une nouvelle implémentation.
 */
public interface JobAssistant {

    /**
     * Lit le texte brut d'une offre (copié depuis un site carrière, LinkedIn...) et en extrait l'essentiel.
     *
     * @throws AiUnavailableException si l'IA ne répond pas
     */
    OfferExtraction extract(String offerText);

    /**
     * Compare l'offre au profil : note, points forts, manques, conseils et brouillon de lettre.
     *
     * @throws AiUnavailableException si l'IA ne répond pas
     */
    MatchAnalysis analyze(CandidateProfile profile, JobOffer offer);
}
