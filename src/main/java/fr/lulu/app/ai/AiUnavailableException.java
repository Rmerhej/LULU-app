package fr.lulu.app.ai;

/**
 * L'IA n'a pas pu répondre : clé absente, limite atteinte, modèle saturé, réponse illisible...
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
