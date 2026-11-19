package fr.lulu.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
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
}
