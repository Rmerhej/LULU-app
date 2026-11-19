package fr.lulu.app.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Réponse de GET /v1/api/jobs/{pays}/search/{page} (seuls les champs utiles sont déclarés).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdzunaSearchResponse(
        Long count,
        List<AdzunaJob> results
) {

    public List<AdzunaJob> safeResults() {
        return results == null ? List.of() : results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdzunaJob(
            String id,
            String title,
            String description,
            @JsonProperty("redirect_url") String redirectUrl,
            String created,
            Company company,
            Location location,
            @JsonProperty("contract_type") String contractType,
            @JsonProperty("contract_time") String contractTime,
            @JsonProperty("salary_min") Double salaryMin,
            @JsonProperty("salary_max") Double salaryMax
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(@JsonProperty("display_name") String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(@JsonProperty("display_name") String displayName) {
    }
}
