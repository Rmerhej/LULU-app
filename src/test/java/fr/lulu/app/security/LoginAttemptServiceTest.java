package fr.lulu.app.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    /** Horloge qu'on peut avancer à la main. */
    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-20T10:00:00Z");
        void advance(Duration duration) { now = now.plus(duration); }
        @Override public Instant instant() { return now; }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private final MovableClock clock = new MovableClock();
    private final LoginAttemptService service = new LoginAttemptService(clock);

    @Test
    void bloqueApresCinqEchecs() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES - 1; i++) {
            service.loginFailed("1.2.3.4");
        }
        assertThat(service.isBlocked("1.2.3.4")).isFalse();
        service.loginFailed("1.2.3.4");
        assertThat(service.isBlocked("1.2.3.4")).isTrue();
        assertThat(service.isBlocked("5.6.7.8")).as("une autre adresse n'est pas touchée").isFalse();
    }

    @Test
    void debloqueApresQuinzeMinutes() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            service.loginFailed("1.2.3.4");
        }
        clock.advance(LoginAttemptService.BLOCK_DURATION.plusSeconds(1));
        assertThat(service.isBlocked("1.2.3.4")).isFalse();
    }

    @Test
    void uneConnexionReussieRemetLeCompteurAZero() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES - 1; i++) {
            service.loginFailed("1.2.3.4");
        }
        service.loginSucceeded("1.2.3.4");
        service.loginFailed("1.2.3.4");
        assertThat(service.isBlocked("1.2.3.4")).isFalse();
    }
}
