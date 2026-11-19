package fr.lulu.app.dto;

import java.util.List;

/**
 * Une page de résultats Adzuna, prête à afficher.
 */
public record SearchResult(List<Candidate> candidates, int page, int totalPages, long totalResults) {

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * Une offre trouvée.
     *
     * @param alreadyImported vrai si l'offre est déjà dans "Mes offres"
     */
    public record Candidate(
            String externalId,
            String title,
            String company,
            String location,
            String contract,
            String salary,
            String excerpt,
            String url,
            String published,
            boolean alreadyImported
    ) {
    }
}
