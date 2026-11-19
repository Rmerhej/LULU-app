package fr.lulu.app.enums;

import java.util.List;

/**
 * Familles de postes visées dans le luxe et la mode, et les expressions de recherche associées
 * (en français et en anglais : le luxe recrute souvent en anglais).
 * Pour ajouter une expression, il suffit de compléter la liste.
 */
public enum JobFamily {

    DEVELOPPEMENT_PRODUIT("Développement produit et matières", List.of(
            "développement produit",
            "assistant développement produit",
            "product developer",
            "développement matières",
            "sourcing matières",
            "trims")),

    CHEF_DE_PRODUIT("Chef de produit", List.of(
            "chef de produit mode",
            "chef de produit junior",
            "assistant chef de produit",
            "product manager fashion")),

    ACHAT("Achat", List.of(
            "acheteur mode",
            "acheteur textile",
            "assistant acheteur",
            "fashion buyer")),

    PRODUCTION("Production", List.of(
            "chef de production mode",
            "assistant production textile",
            "coordinateur production",
            "production manager fashion"));

    private final String libelle;
    private final List<String> expressions;

    JobFamily(String libelle, List<String> expressions) {
        this.libelle = libelle;
        this.expressions = expressions;
    }

    public String getLibelle() {
        return libelle;
    }

    public List<String> getExpressions() {
        return expressions;
    }
}
