package fr.lulu.app.service;

import fr.lulu.app.ai.AiUnavailableException;
import fr.lulu.app.ai.JobAssistant;
import fr.lulu.app.ai.MatchAnalysis;
import fr.lulu.app.ai.OfferExtraction;
import fr.lulu.app.dto.ManualOfferForm;
import fr.lulu.app.dto.TrackingForm;
import fr.lulu.app.entity.CandidateProfile;
import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.enums.OfferSource;
import fr.lulu.app.repository.JobOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private JobOfferRepository repository;

    @Mock
    private JobAssistant assistant;

    @Mock
    private ProfileService profileService;

    private OfferService service;

    @BeforeEach
    void setUp() {
        service = new OfferService(repository, assistant, profileService);
    }

    @Test
    void ajoutManuelAvecLectureParLIa() {
        when(assistant.extract(any())).thenReturn(
                new OfferExtraction("Développeur matières", "Maison Exemple", "Paris", "CDI"));
        when(repository.save(any(JobOffer.class))).thenAnswer(i -> i.getArgument(0));

        OfferService.ManualAddResult result = service.addManual(form("https://carrieres.exemple.fr/1"));

        JobOffer offer = result.offer();
        assertNull(result.warning());
        assertEquals(OfferSource.MANUELLE, offer.getSource());
        assertEquals("Développeur matières", offer.getTitle());
        assertEquals("Maison Exemple", offer.getCompany());
        assertEquals("https://carrieres.exemple.fr/1", offer.getUrl());
        assertTrue(offer.getDescription().startsWith("Développeur matières H/F"));
    }

    @Test
    void sansIaLOffreEstQuandMemeEnregistree() {
        when(assistant.extract(any())).thenThrow(new AiUnavailableException("saturé"));
        when(repository.save(any(JobOffer.class))).thenAnswer(i -> i.getArgument(0));

        OfferService.ManualAddResult result = service.addManual(form(""));

        assertEquals("Développeur matières H/F", result.offer().getTitle(), "Titre = première ligne du texte");
        assertNull(result.offer().getUrl());
        assertNotNull(result.warning());
    }

    @Test
    void analyseImpossibleSansProfilComplet() {
        when(repository.findById(1L)).thenReturn(Optional.of(new JobOffer()));
        when(profileService.get()).thenReturn(new CandidateProfile());

        assertThrows(AiUnavailableException.class, () -> service.analyze(1L));
        verifyNoInteractions(assistant);
    }

    @Test
    void analyseEnregistreLeResultatEtSigneLaLettre() {
        JobOffer offer = new JobOffer();
        CandidateProfile profile = completeProfile();
        when(repository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileService.get()).thenReturn(profile);
        when(assistant.analyze(profile, offer)).thenReturn(
                new MatchAnalysis(130, "PLM, prototypes", "Pas de cuir", "Citer Lemaire", "Madame, Monsieur,\nTexte."));
        when(repository.save(any(JobOffer.class))).thenAnswer(i -> i.getArgument(0));

        JobOffer analysed = service.analyze(1L);

        assertEquals(100, analysed.getAiScore(), "Note bornée à 100");
        assertEquals("PLM, prototypes", analysed.getAiStrengths());
        assertTrue(analysed.getCoverLetter().endsWith("Lucile Exemple"), "Nom ajouté par le code");
        assertNotNull(analysed.getAnalysedAt());
    }

    @Test
    void premierEnvoiDateAutomatiquement() {
        JobOffer offer = new JobOffer();
        when(repository.findById(1L)).thenReturn(Optional.of(offer));

        TrackingForm form = new TrackingForm();
        form.setStatus(ApplicationStatus.POSTULEE);
        service.track(1L, form);

        assertEquals(ApplicationStatus.POSTULEE, offer.getStatus());
        assertEquals(LocalDate.now(), offer.getAppliedAt());
    }

    @Test
    void etapeNonEnvoyeeNeDatePas() {
        JobOffer offer = new JobOffer();
        when(repository.findById(1L)).thenReturn(Optional.of(offer));

        TrackingForm form = new TrackingForm();
        form.setStatus(ApplicationStatus.A_POSTULER);
        service.track(1L, form);

        assertNull(offer.getAppliedAt());
    }

    @Test
    void signatureSeulementSiNomRenseigne() {
        assertEquals("Texte", OfferService.signLetter("  Texte ", null));
        assertEquals("Texte\n\nLucile", OfferService.signLetter("Texte", "Lucile"));
    }

    // ----- Données de test -----

    private static ManualOfferForm form(String url) {
        ManualOfferForm form = new ManualOfferForm();
        form.setUrl(url);
        form.setText("Développeur matières H/F\nMaison Exemple recrute pour son studio parisien...");
        return form;
    }

    private static CandidateProfile completeProfile() {
        CandidateProfile profile = new CandidateProfile();
        profile.setFullName("Lucile Exemple");
        profile.setHeadline("Assistante développement produit");
        profile.setExperience("Alternance en développement prêt-à-porter.");
        return profile;
    }
}
