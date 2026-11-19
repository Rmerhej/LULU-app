package fr.lulu.app.config;

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

/**
 * Toutes les pages demandent une connexion (un seul utilisateur, défini dans .env).
 * Mot de passe haché (BCrypt), protection CSRF active sur tous les formulaires.
 */
@Configuration
public class SecurityConfig {

    static final int MIN_PASSWORD_LENGTH = 8;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // Toujours revenir à l'accueil (évite un retour vers /favicon.ico ou /error)
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
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
