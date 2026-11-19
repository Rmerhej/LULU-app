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
import fr.lulu.app.exception.OfferNotFoundException;
import fr.lulu.app.repository.JobOfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Offres enregistrées : ajout manuel, analyse IA, lettre, suivi.
 * Les appels à l'IA se font hors transaction (ils peuvent durer plusieurs secondes).
 */
@Service
public class OfferService {

    private final JobOfferRepository repository;
    private final JobAssistant assistant;
    private final ProfileService profileService;

    public OfferService(JobOfferRepository repository, JobAssistant assistant, ProfileService profileService) {
        this.repository = repository;
        this.assistant = assistant;
        this.profileService = profileService;
    }

    public List<JobOffer> list(ApplicationStatus status) {
        return status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);
    }

    public JobOffer get(Long id) {
        return repository.findById(id).orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Ajoute une offre collée à la main. L'IA en extrait le titre, la maison, le lieu et le contrat.
     * Si l'IA ne répond pas, l'offre est quand même enregistrée (titre = première ligne du texte).
     *
     * @return le résultat, avec un éventuel avertissement à afficher
     */
    public ManualAddResult addManual(ManualOfferForm form) {
        String text = form.getText().strip();

        JobOffer offer = new JobOffer();
        offer.setSource(OfferSource.MANUELLE);
        offer.setUrl(blankToNull(form.getUrl()));
        offer.setDescription(text);

        String warning = null;
        try {
            OfferExtraction extraction = assistant.extract(text);
            offer.setTitle(truncate(defaultIfBlank(extraction.titre(), firstLine(text)), 255));
            offer.setCompany(truncate(blankToNull(extraction.entreprise()), 255));
            offer.setLocation(truncate(blankToNull(extraction.lieu()), 255));
            offer.setContractType(truncate(blankToNull(extraction.typeContrat()), 100));
        } catch (AiUnavailableException e) {
            offer.setTitle(truncate(firstLine(text), 255));
            warning = "Offre enregistrée, mais sans lecture automatique : " + e.getMessage();
        }

        return new ManualAddResult(repository.save(offer), warning);
    }

    public record ManualAddResult(JobOffer offer, String warning) {
    }

    /** Compare l'offre au profil et enregistre l'analyse et le brouillon de lettre. */
    public JobOffer analyze(Long id) {
        JobOffer offer = get(id);
        CandidateProfile profile = profileService.get();
        if (!profile.isComplete()) {
            throw new AiUnavailableException(
                    "Complétez d'abord le profil (poste recherché et expériences) : l'analyse compare l'offre au profil.");
        }

        MatchAnalysis analysis = assistant.analyze(profile, offer);

        offer.setAiScore(Math.clamp(analysis.score(), 0, 100));
        offer.setAiStrengths(truncate(analysis.pointsForts(), 2000));
        offer.setAiGaps(truncate(analysis.manques(), 2000));
        offer.setAiAdvice(truncate(analysis.conseils(), 2000));
        offer.setCoverLetter(truncate(signLetter(analysis.lettre(), profile.getFullName()), 20000));
        offer.setAnalysedAt(LocalDateTime.now());
        return repository.save(offer);
    }

    /** Le nom est ajouté par le code : l'IA ne le reçoit jamais. */
    static String signLetter(String letter, String fullName) {
        String body = letter == null ? "" : letter.strip();
        return fullName == null || fullName.isBlank() ? body : body + "\n\n" + fullName.strip();
    }

    public void saveLetter(Long id, String letter) {
        JobOffer offer = get(id);
        offer.setCoverLetter(truncate(letter == null ? null : letter.strip(), 20000));
        repository.save(offer);
    }

    /** Remplace la description (utile quand Adzuna ne fournit qu'un extrait). */
    public void saveDescription(Long id, String description) {
        JobOffer offer = get(id);
        offer.setDescription(description == null ? null : description.strip());
        repository.save(offer);
    }

    public void track(Long id, TrackingForm form) {
        JobOffer offer = get(id);
        offer.setStatus(form.getStatus());
        offer.setNotes(blankToNull(form.getNotes()));

        LocalDate appliedAt = form.getAppliedAt();
        // Première fois que la candidature est envoyée sans date : on met la date du jour
        if (appliedAt == null && form.getStatus().isEnvoyee() && offer.getAppliedAt() == null) {
            appliedAt = LocalDate.now();
        }
        offer.setAppliedAt(appliedAt != null ? appliedAt : offer.getAppliedAt());
        repository.save(offer);
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }

    // ----- Outils -----

    static String firstLine(String text) {
        return text.lines().map(String::strip).filter(line -> !line.isEmpty()).findFirst().orElse("Offre");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
