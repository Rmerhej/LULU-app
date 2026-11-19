package fr.lulu.app.service;

import fr.lulu.app.dto.DashboardStats;
import fr.lulu.app.dto.DashboardStats.StatusCount;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.repository.JobOfferRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JobOfferRepository repository;

    public DashboardService(JobOfferRepository repository) {
        this.repository = repository;
    }

    public DashboardStats compute() {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : repository.countByStatus()) {
            if (row[0] instanceof ApplicationStatus status) {
                counts.put(status, ((Number) row[1]).longValue());
            }
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long envoyees = counts.entrySet().stream()
                .filter(e -> e.getKey().isEnvoyee())
                .mapToLong(Map.Entry::getValue).sum();
        long entretiens = counts.getOrDefault(ApplicationStatus.ENTRETIEN, 0L)
                + counts.getOrDefault(ApplicationStatus.OFFRE, 0L);

        List<StatusCount> parEtape = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            parEtape.add(new StatusCount(status.getLibelle(), status.getCss(), counts.getOrDefault(status, 0L)));
        }

        return new DashboardStats(
                total,
                repository.countByAnalysedAtIsNotNull(),
                envoyees,
                entretiens,
                percentOrNull(entretiens, envoyees),
                parEtape);
    }

    static Integer percentOrNull(long part, long whole) {
        return whole == 0 ? null : (int) Math.round(part * 100.0 / whole);
    }
}
