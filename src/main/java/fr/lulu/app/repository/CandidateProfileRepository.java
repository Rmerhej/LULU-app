package fr.lulu.app.repository;

import fr.lulu.app.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    /** L'application ne gère qu'un profil : le premier enregistré. */
    Optional<CandidateProfile> findFirstByOrderByIdAsc();
}
