package fr.lulu.app.service;

import fr.lulu.app.client.AdzunaClient;
import fr.lulu.app.client.dto.AdzunaSearchResponse;
import fr.lulu.app.client.dto.AdzunaSearchResponse.AdzunaJob;
import fr.lulu.app.dto.SearchResult;
import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.enums.OfferSource;
import fr.lulu.app.repository.JobOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSearchServiceTest {

    @Mock
    private AdzunaClient client;

    @Mock
    private JobOfferRepository repository;

    private JobSearchService service;

    @BeforeEach
    void setUp() {
        service = new JobSearchService(client, repository);
    }

    @Test
    void rechercheSignaleLesOffresDejaAjoutees() {
        when(client.search("trims", "Paris", 1)).thenReturn(response(45, job("1", "A"), job("2", "B")));
        when(repository.findExistingExternalIds(eq(OfferSource.ADZUNA), anyCollection())).thenReturn(Set.of("2"));

        SearchResult result = service.search("trims", "Paris", 1);

        assertEquals(2, result.candidates().size());
        assertFalse(result.candidates().get(0).alreadyImported());
        assertTrue(result.candidates().get(1).alreadyImported());
        assertEquals(3, result.totalPages(), "45 offres / 20 par page = 3 pages");
        assertEquals("CDI, temps plein", result.candidates().getFirst().contract());
    }

    @Test
    void importNeGardeQueLesOffresCocheesEtNouvelles() {
        when(client.search("acheteur mode", null, 1))
                .thenReturn(response(3, job("1", "Acheteur"), job("2", "Déjà là"), job("3", "Non cochée")));
        when(repository.findExistingExternalIds(eq(OfferSource.ADZUNA), anyCollection())).thenReturn(Set.of("2"));

        int imported = service.importSelected("acheteur mode", null, 1, List.of("1", "2", "<script>"));

        assertEquals(1, imported);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<JobOffer>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        JobOffer saved = captor.getValue().getFirst();
        assertEquals("1", saved.getExternalId());
        assertEquals("Acheteur", saved.getTitle());
        assertEquals(OfferSource.ADZUNA, saved.getSource());
        assertEquals(ApplicationStatus.A_ETUDIER, saved.getStatus());
        assertEquals("38–42 k€ par an", saved.getSalary());
        assertNotNull(saved.getPublishedAt());
    }

    @Test
    void sansSelectionAucunAppel() {
        assertEquals(0, service.importSelected("trims", null, 1, List.of()));
        verifyNoInteractions(client, repository);
    }

    @Test
    void lesBalisesHtmlDAdzunaSontRetirees() {
        assertEquals("Chef de produit & matières",
                JobSearchService.stripHtml("Chef de <strong>produit</strong> &amp; matières"));
    }

    @Test
    void dateInvalideIgnoree() {
        assertNull(JobSearchService.parseDate("pas une date"));
        assertNotNull(JobSearchService.parseDate("2026-11-18T09:30:00Z"));
    }

    // ----- Données de test -----

    private static AdzunaJob job(String id, String title) {
        return new AdzunaJob(id, title, "Description", "https://www.adzuna.fr/details/" + id,
                "2026-11-18T09:30:00Z",
                new AdzunaSearchResponse.Company("Maison Exemple"),
                new AdzunaSearchResponse.Location("Paris"),
                "permanent", "full_time", 38000.0, 42000.0);
    }

    private static AdzunaSearchResponse response(long count, AdzunaJob... jobs) {
        return new AdzunaSearchResponse(count, List.of(jobs));
    }
}
