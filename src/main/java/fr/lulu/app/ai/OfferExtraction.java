package fr.lulu.app.ai;

/**
 * Informations extraites par l'IA du texte d'une offre collée à la main.
 * Les champs inconnus sont laissés vides par l'IA.
 */
public record OfferExtraction(
        String titre,
        String entreprise,
        String lieu,
        String typeContrat
) {
}
