package fr.lulu.app.dto;

import java.util.List;

/**
 * Chiffres de l'accueil.
 *
 * @param tauxReponse part des candidatures envoyées qui ont abouti à un entretien ou une offre (ou null)
 */
public record DashboardStats(
        long total,
        long analysees,
        long envoyees,
        long entretiens,
        Integer tauxReponse,
        List<StatusCount> parEtape
) {

    public record StatusCount(String libelle, String css, long nombre) {
    }
}
