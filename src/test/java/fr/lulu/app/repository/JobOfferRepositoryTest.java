package fr.lulu.app.repository;

import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.enums.OfferSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JobOfferRepositoryTest {

    @Autowired
    private JobOfferRepository repository;

    @Test
    void retrouveLesOffresAdzunaDejaImportees() {
        repository.save(offer(OfferSource.ADZUNA, "111"));
        repository.save(offer(OfferSource.MANUELLE, null));

        Set<String> existing = repository.findExistingExternalIds(OfferSource.ADZUNA, List.of("111", "222"));

        assertEquals(Set.of("111"), existing);
    }

    @Test
    void uneOffreAdzunaNePeutPasEtreImporteeDeuxFois() {
        repository.saveAndFlush(offer(OfferSource.ADZUNA, "111"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(offer(OfferSource.ADZUNA, "111")));
    }

    @Test
    void plusieursOffresManuellesSansIdentifiant() {
        repository.saveAndFlush(offer(OfferSource.MANUELLE, null));
        repository.saveAndFlush(offer(OfferSource.MANUELLE, null));

        assertEquals(2, repository.count());
    }

    @Test
    void compteParEtape() {
        repository.save(offer(OfferSource.MANUELLE, null));
        JobOffer sent = offer(OfferSource.MANUELLE, null);
        sent.setStatus(ApplicationStatus.POSTULEE);
        repository.save(sent);

        List<Object[]> rows = repository.countByStatus();

        assertEquals(2, rows.size());
    }

    private static JobOffer offer(OfferSource source, String externalId) {
        JobOffer offer = new JobOffer();
        offer.setSource(source);
        offer.setExternalId(externalId);
        offer.setTitle("Développement produit");
        return offer;
    }
}
