package fr.lulu.app.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Placé avant le formulaire de connexion : une adresse bloquée ne peut même plus essayer.
 * Créé dans SecurityConfig (pas de @Component, sinon il serait appliqué deux fois).
 */
public class LoginThrottleFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttempts;

    public LoginThrottleFilter(LoginAttemptService loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (loginAttempts.isBlocked(request.getRemoteAddr())) {
            response.sendRedirect(request.getContextPath() + "/login?bloque");
            return;
        }
        chain.doFilter(request, response);
    }
}
