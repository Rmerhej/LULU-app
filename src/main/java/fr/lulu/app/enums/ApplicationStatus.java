package fr.lulu.app.enums;

/**
 * Étapes du suivi d'une candidature, dans l'ordre.
 */
public enum ApplicationStatus {

    A_ETUDIER("À étudier", false),
    A_POSTULER("À postuler", false),
    POSTULEE("Candidature envoyée", true),
    RELANCEE("Relancée", true),
    ENTRETIEN("Entretien", true),
    OFFRE("Offre reçue", true),
    REFUSEE("Refusée", true),
    ABANDONNEE("Abandonnée", false);

    private final String libelle;
    /** Vrai si la candidature a été envoyée (sert au taux de réponse). */
    private final boolean envoyee;

    ApplicationStatus(String libelle, boolean envoyee) {
        this.libelle = libelle;
        this.envoyee = envoyee;
    }

    public String getLibelle() {
        return libelle;
    }

    public boolean isEnvoyee() {
        return envoyee;
    }

    /** Nom de classe CSS de l'étiquette (couleur selon l'étape). */
    public String getCss() {
        return switch (this) {
            case A_ETUDIER, A_POSTULER -> "tag-todo";
            case POSTULEE, RELANCEE -> "tag-sent";
            case ENTRETIEN, OFFRE -> "tag-hot";
            case REFUSEE, ABANDONNEE -> "tag-closed";
        };
    }
}
