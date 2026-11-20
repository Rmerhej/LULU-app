package fr.lulu.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sécurité et pages principales, de bout en bout (vraie configuration, base H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unePageProtegeeRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/offres"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void connexionAvecLeBonMotDePasse() throws Exception {
        mockMvc.perform(formLogin().user("lulu").password("motdepasse-test"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void connexionRefuseeAvecUnMauvaisMotDePasse() throws Exception {
        mockMvc.perform(formLogin().user("lulu").password("mauvais"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser
    void lesPagesPrincipalesSAffichent() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("offres suivies")));
        mockMvc.perform(get("/profil")).andExpect(status().isOk());
        mockMvc.perform(get("/offres")).andExpect(status().isOk());
        mockMvc.perform(get("/offres/nouvelle")).andExpect(status().isOk());
        mockMvc.perform(get("/recherche")).andExpect(status().isOk())
                .andExpect(content().string(containsString("développement produit")));
    }

    @Test
    @WithMockUser
    void offreInexistante() throws Exception {
        mockMvc.perform(get("/offres/999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void formulaireSansJetonCsrfRefuse() throws Exception {
        mockMvc.perform(post("/offres/1/supprimer")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void profilInvalideReaffiche() throws Exception {
        mockMvc.perform(post("/profil").with(csrf()).param("headline", "").param("experience", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Indiquez le poste recherché")));
    }

    @Test
    void lesFichiersDeLApplicationInstallableSontPublics() throws Exception {
        mockMvc.perform(get("/manifest.json")).andExpect(status().isOk())
                .andExpect(content().string(containsString("\"short_name\": \"LULU\"")));
        mockMvc.perform(get("/sw.js")).andExpect(status().isOk());
        mockMvc.perform(get("/offline.html")).andExpect(status().isOk());
        mockMvc.perform(get("/icons/icon-192.png")).andExpect(status().isOk());
    }

    @Test
    void enTetesDeSecurite() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(header().string("Content-Security-Policy", startsWith("default-src 'self'")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    void hstsEnHttps() throws Exception {
        mockMvc.perform(get("/login").secure(true))
                .andExpect(header().string("Strict-Transport-Security", startsWith("max-age=31536000")));
    }

    @Test
    void resterConnecteeDeposeUnCookie() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "lulu").param("password", "motdepasse-test")
                        .param("remember-me", "on"))
                .andExpect(authenticated())
                .andExpect(cookie().exists("remember-me"))
                .andExpect(cookie().httpOnly("remember-me", true));
    }

    @Test
    void adresseBloqueeApresCinqEchecs() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login").with(csrf()).with(depuis("10.9.9.9"))
                            .param("username", "lulu").param("password", "mauvais"))
                    .andExpect(unauthenticated());
        }
        // Même le bon mot de passe est refusé tant que le blocage dure
        mockMvc.perform(post("/login").with(csrf()).with(depuis("10.9.9.9"))
                        .param("username", "lulu").param("password", "motdepasse-test"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?bloque"));
        // Une autre adresse peut toujours se connecter
        mockMvc.perform(post("/login").with(csrf()).with(depuis("10.8.8.8"))
                        .param("username", "lulu").param("password", "motdepasse-test"))
                .andExpect(authenticated());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor depuis(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }
}
