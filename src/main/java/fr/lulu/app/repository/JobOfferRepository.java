package fr.lulu.app.repository;

import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.enums.OfferSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    List<JobOffer> findAllByOrderByCreatedAtDesc();

    List<JobOffer> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    List<JobOffer> findTop5ByOrderByCreatedAtDesc();

    /** Parmi les identifiants donnés, ceux déjà importés pour cette source (une seule requête). */
    @Query("select o.externalId from JobOffer o where o.source = :source and o.externalId in :ids")
    Set<String> findExistingExternalIds(@Param("source") OfferSource source, @Param("ids") Collection<String> ids);

    /** Une ligne [statut, nombre] par statut. */
    @Query("select o.status, count(o) from JobOffer o group by o.status")
    List<Object[]> countByStatus();

    long countByAnalysedAtIsNotNull();
}
