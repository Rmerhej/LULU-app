package fr.lulu.app.config;

import java.time.Duration;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import fr.lulu.app.security.LoginAttemptService;
import fr.lulu.app.security.LoginThrottleFilter;

/**
 * Toutes les pages demandent une connexion (un seul utilisateur, défini dans .env).
 * - Mot de passe haché (BCrypt), protection CSRF sur tous les formulaires.
 * - Blocage de 15 minutes après 5 essais ratés depuis la même adresse.
 * - "Rester connectée" : jeton stocké en base (table persistent_logins), valable 30 jours,
 *   supprimé à la déconnexion.
 * - En-têtes de sécurité : CSP stricte, HSTS (en HTTPS), anti-iframe, Referrer-Policy.
 */
@Configuration
public class SecurityConfig {

    static final int MIN_PASSWORD_LENGTH = 8;
    static final Duration REMEMBER_ME = Duration.ofDays(30);

    /** Seules sources autorisées : le site lui-même, Google Fonts et le CDN de Bootstrap. */
    static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' https://fonts.googleapis.com https://cdn.jsdelivr.net",
            "font-src 'self' https://fonts.gstatic.com",
            "img-src 'self' data:",
            "connect-src 'self' https://fonts.googleapis.com https://fonts.gstatic.com https://cdn.jsdelivr.net",
            "manifest-src 'self'",
            "worker-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'");

    /** Fichiers publics : connexion, mise en page et fichiers de l'application installable (PWA). */
    private static final String[] PUBLIC_PATHS = {
            "/login", "/css/**", "/js/**", "/icons/**", "/favicon.ico", "/error",
            "/manifest.json", "/sw.js", "/offline.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LoginAttemptService loginAttempts,
                                                   PersistentTokenRepository tokenRepository,
                                                   UserDetailsService userDetailsService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new LoginThrottleFilter(loginAttempts), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        // Toujours revenir à l'accueil (évite un retour vers /favicon.ico ou /error)
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .rememberMe(remember -> remember
                        .tokenRepository(tokenRepository)
                        .userDetailsService(userDetailsService)
                        .key(UUID.randomUUID().toString())
                        .tokenValiditySeconds((int) REMEMBER_ME.toSeconds()))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(Duration.ofDays(365).toSeconds())
                                .includeSubDomains(false)));
        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${lulu.security.username}") String username,
            @Value("${lulu.security.password:}") String password,
            PasswordEncoder passwordEncoder) {

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "APP_PASSWORD absent ou trop court (" + MIN_PASSWORD_LENGTH
                            + " caractères minimum) : ajoutez-le dans le fichier .env, puis relancez l'application.");
        }

        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());
    }
}
