package fr.lulu.app.enums;

public enum OfferSource {

    ADZUNA("Adzuna"),
    MANUELLE("Ajoutée à la main");

    private final String libelle;

    OfferSource(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
