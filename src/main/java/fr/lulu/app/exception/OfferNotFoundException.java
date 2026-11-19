package fr.lulu.app.exception;

public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(Long id) {
        super("L'offre n°" + id + " est introuvable.");
    }
}
