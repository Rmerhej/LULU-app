package fr.lulu.app.ai;

/**
 * Analyse de l'adéquation entre une offre et le profil, renvoyée par l'IA.
 *
 * @param score       0 à 100 : adéquation du profil avec l'offre
 * @param pointsForts ce qui, dans le profil, correspond à l'offre
 * @param manques     ce qui manque ou est plus faible
 * @param conseils    comment présenter la candidature
 * @param lettre      brouillon de lettre de motivation, dans la langue de l'offre, sans signature
 */
public record MatchAnalysis(
        int score,
        String pointsForts,
        String manques,
        String conseils,
        String lettre
) {
}
