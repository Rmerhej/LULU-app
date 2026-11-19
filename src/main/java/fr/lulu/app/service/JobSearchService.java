package fr.lulu.app.service;

import fr.lulu.app.client.AdzunaClient;
import fr.lulu.app.client.dto.AdzunaSearchResponse;
import fr.lulu.app.client.dto.AdzunaSearchResponse.AdzunaJob;
import fr.lulu.app.dto.SearchResult;
import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.OfferSource;
import fr.lulu.app.repository.JobOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recherche d'offres chez Adzuna et import des offres choisies dans "Mes offres".
 */
@Service
public class JobSearchService {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AdzunaClient client;
    private final JobOfferRepository repository;

    public JobSearchService(AdzunaClient client, JobOfferRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public SearchResult search(String expression, String where, int page) {
        int safePage = clampPage(page);
        AdzunaSearchResponse response = client.search(expression, where, safePage);
        List<AdzunaJob> jobs = response == null ? List.of() : response.safeResults();

        Set<String> existing = findExisting(jobs.stream().map(AdzunaJob::id).toList());

        List<SearchResult.Candidate> candidates = jobs.stream()
                .map(job -> toCandidate(job, existing.contains(job.id())))
                .toList();

        long total = response == null || response.count() == null ? 0 : response.count();
        int totalPages = (int) Math.min(AdzunaClient.MAX_PAGE,
                (total + AdzunaClient.PER_PAGE - 1) / AdzunaClient.PER_PAGE);

        return new SearchResult(candidates, safePage, totalPages, total);
    }

    /**
     * Importe les offres cochées. Seuls les identifiants viennent du navigateur :
     * les données sont relues chez Adzuna (même recherche, même page).
     *
     * @return le nombre d'offres réellement ajoutées
     */
    @Transactional
    public int importSelected(String expression, String where, int page, Collection<String> selectedIds) {
        Set<String> wanted = selectedIds == null ? Set.of() : selectedIds.stream()
                .filter(id -> id != null && id.matches("^[0-9A-Za-z_-]{1,100}$"))
                .collect(Collectors.toSet());
        if (wanted.isEmpty()) {
            return 0;
        }

        AdzunaSearchResponse response = client.search(expression, where, clampPage(page));
        List<AdzunaJob> selected = (response == null ? List.<AdzunaJob>of() : response.safeResults()).stream()
                .filter(job -> wanted.contains(job.id()))
                .toList();

        Set<String> existing = findExisting(selected.stream().map(AdzunaJob::id).toList());

        List<JobOffer> toSave = new ArrayList<>();
        for (AdzunaJob job : selected) {
            if (!existing.contains(job.id())) {
                toSave.add(toOffer(job));
            }
        }
        repository.saveAll(toSave);
        return toSave.size();
    }

    // ----- Conversions -----

    private SearchResult.Candidate toCandidate(AdzunaJob job, boolean alreadyImported) {
        LocalDateTime published = parseDate(job.created());
        return new SearchResult.Candidate(
                job.id(),
                stripHtml(job.title()),
                job.company() == null ? null : job.company().displayName(),
                job.location() == null ? null : job.location().displayName(),
                contract(job),
                salary(job),
                excerpt(stripHtml(job.description()), 220),
                job.redirectUrl(),
                published == null ? null : published.format(DISPLAY_DATE),
                alreadyImported);
    }

    JobOffer toOffer(AdzunaJob job) {
        JobOffer offer = new JobOffer();
        offer.setSource(OfferSource.ADZUNA);
        offer.setExternalId(job.id());
        offer.setTitle(truncate(defaultIfBlank(stripHtml(job.title()), "Offre sans titre"), 255));
        offer.setCompany(truncate(job.company() == null ? null : job.company().displayName(), 255));
        offer.setLocation(truncate(job.location() == null ? null : job.location().displayName(), 255));
        offer.setContractType(truncate(contract(job), 100));
        offer.setSalary(truncate(salary(job), 100));
        offer.setUrl(truncate(job.redirectUrl(), 1000));
        offer.setDescription(stripHtml(job.description()));
        offer.setPublishedAt(parseDate(job.created()));
        return offer;
    }

    /** Adzuna entoure les mots recherchés de balises &lt;strong&gt; : on les retire. */
    static String stripHtml(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("<[^>]*>", "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .strip();
    }

    static String contract(AdzunaJob job) {
        String type = job.contractType() == null ? null : switch (job.contractType()) {
            case "permanent" -> "CDI";
            case "contract" -> "CDD / mission";
            default -> job.contractType();
        };
        String time = job.contractTime() == null ? null : switch (job.contractTime()) {
            case "full_time" -> "temps plein";
            case "part_time" -> "temps partiel";
            default -> job.contractTime();
        };
        if (type == null) {
            return time;
        }
        return time == null ? type : type + ", " + time;
    }

    static String salary(AdzunaJob job) {
        if (job.salaryMin() == null && job.salaryMax() == null) {
            return null;
        }
        if (job.salaryMin() != null && job.salaryMax() != null && !job.salaryMin().equals(job.salaryMax())) {
            return thousands(job.salaryMin()) + "–" + thousands(job.salaryMax()) + " k€ par an";
        }
        return thousands(job.salaryMin() != null ? job.salaryMin() : job.salaryMax()) + " k€ par an";
    }

    private static long thousands(double value) {
        return Math.round(value / 1000);
    }

    static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Europe/Paris")).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Set<String> findExisting(List<String> ids) {
        List<String> valid = ids.stream().filter(id -> id != null && !id.isBlank()).toList();
        return valid.isEmpty() ? Set.of() : repository.findExistingExternalIds(OfferSource.ADZUNA, valid);
    }

    private static int clampPage(int page) {
        return Math.min(Math.max(page, 1), AdzunaClient.MAX_PAGE);
    }

    private static String excerpt(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max).strip() + "…";
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
