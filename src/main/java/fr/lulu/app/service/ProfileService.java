package fr.lulu.app.service;

import fr.lulu.app.dto.ProfileForm;
import fr.lulu.app.entity.CandidateProfile;
import fr.lulu.app.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;

/**
 * Le profil unique de la candidate.
 */
@Service
public class ProfileService {

    private final CandidateProfileRepository repository;

    public ProfileService(CandidateProfileRepository repository) {
        this.repository = repository;
    }

    /** Le profil enregistré, ou un profil vide (non enregistré) s'il n'existe pas encore. */
    public CandidateProfile get() {
        return repository.findFirstByOrderByIdAsc().orElseGet(CandidateProfile::new);
    }

    public CandidateProfile save(ProfileForm form) {
        CandidateProfile profile = get();
        form.applyTo(profile);
        return repository.save(profile);
    }
}
