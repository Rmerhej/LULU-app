package fr.lulu.app.client;

/**
 * Une API externe n'a pas répondu correctement, ou n'est pas configurée.
 */
public class ApiUnavailableException extends RuntimeException {

    public ApiUnavailableException(String message) {
        super(message);
    }

    public ApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
