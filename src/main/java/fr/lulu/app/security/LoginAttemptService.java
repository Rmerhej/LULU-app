package fr.lulu.app.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

/**
 * Protection contre les essais de mots de passe en série.
 * Après MAX_FAILURES échecs depuis la même adresse IP, cette adresse est bloquée pendant BLOCK_DURATION.
 * On bloque par adresse IP et non par identifiant : un inconnu ne peut pas empêcher Lucile de se connecter.
 */
@Service
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    /** Garde-fou mémoire : au-delà, on oublie les plus anciennes adresses. */
    private static final int MAX_TRACKED = 10_000;

    private record Attempts(int failures, Instant lastFailure) { }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String ip) {
        Attempts current = attempts.get(ip);
        if (current == null) {
            return false;
        }
        if (expired(current)) {
            attempts.remove(ip);
            return false;
        }
        return current.failures() >= MAX_FAILURES;
    }

    public void loginFailed(String ip) {
        if (attempts.size() >= MAX_TRACKED) {
            attempts.entrySet().removeIf(entry -> expired(entry.getValue()));
        }
        attempts.merge(ip, new Attempts(1, clock.instant()), (old, ignored) ->
                expired(old) ? new Attempts(1, clock.instant()) : new Attempts(old.failures() + 1, clock.instant()));
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    private boolean expired(Attempts value) {
        return value.lastFailure().plus(BLOCK_DURATION).isBefore(clock.instant());
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
            loginFailed(details.getRemoteAddress());
        }
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
            loginSucceeded(details.getRemoteAddress());
        }
    }
}
