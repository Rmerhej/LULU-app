package fr.lulu.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Offre ajoutée à la main : le lien (facultatif) et le texte copié depuis le site.
 */
public class ManualOfferForm {

    @Size(max = 1000, message = "1000 caractères maximum")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le lien doit commencer par http:// ou https://")
    private String url;

    @NotBlank(message = "Collez le texte de l'offre")
    @Size(min = 80, max = 30000, message = "Collez le texte complet de l'offre (80 à 30 000 caractères)")
    private String text;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
