package fr.lulu.app.ai;

import fr.lulu.app.entity.CandidateProfile;
import fr.lulu.app.entity.JobOffer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests sans appel à l'IA : ce qui est envoyé, et les garde-fous.
 */
class GeminiJobAssistantTest {

    @Test
    void leNomDeLaCandidateNEstJamaisEnvoye() {
        CandidateProfile profile = new CandidateProfile();
        profile.setFullName("Lucile Exemple");
        profile.setHeadline("Assistante développement produit");
        profile.setExperience("Alternance chez une maison de prêt-à-porter.");
        JobOffer offer = new JobOffer();
        offer.setTitle("Product Developer");
        offer.setCompany("Maison Exemple");

        String prompt = GeminiJobAssistant.buildAnalysisPrompt(profile, offer);

        assertFalse(prompt.contains("Lucile"));
        assertTrue(prompt.contains("Poste recherché : Assistante développement produit"));
        assertTrue(prompt.contains("Intitulé : Product Developer"));
        assertTrue(prompt.contains("Contrat : non renseigné"));
    }

    @Test
    void textesNettoyesEtBornes() {
        assertEquals("Maison (test)", GeminiJobAssistant.clean("Maison {test}"));
        String long_ = "a".repeat(GeminiJobAssistant.MAX_TEXT + 500);
        assertTrue(GeminiJobAssistant.clean(long_).length() < long_.length());
        assertFalse(GeminiJobAssistant.ANALYSIS_PROMPT.contains("{"));
        assertFalse(GeminiJobAssistant.EXTRACTION_PROMPT.contains("{"));
    }

    @Test
    void sansCleAucunAppel() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        when(builder.build()).thenReturn(chatClient);
        GeminiJobAssistant assistant = new GeminiJobAssistant(builder, GeminiJobAssistant.PLACEHOLDER_KEY);

        AiUnavailableException e = assertThrows(AiUnavailableException.class, () -> assistant.extract("texte"));
        assertTrue(e.getMessage().contains("GEMINI_API_KEY"));
        verifyNoInteractions(chatClient);
    }

    @Test
    void erreurs503Et404DonnentUnConseil() {
        assertTrue(GeminiJobAssistant.describeFailure(
                new RuntimeException("503 The model is overloaded"), "cle").contains("saturé"));
        assertTrue(GeminiJobAssistant.describeFailure(
                new RuntimeException("404 model is no longer available"), "cle").contains("introuvable"));
    }

    @Test
    void laCleNApparaitJamais() {
        String key = "AIzaSyDUMMYDUMMYDUMMYDUMMYDUMMY123";
        String message = GeminiJobAssistant.describeFailure(
                new RuntimeException("Erreur sur https://api.test/models?key=" + key), key);

        assertFalse(message.contains(key));
    }
}
