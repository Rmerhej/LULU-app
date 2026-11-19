package fr.lulu.app.client;

import fr.lulu.app.client.dto.AdzunaSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Client de l'API Adzuna (agrégateur d'offres d'emploi, offre gratuite).
 * Documentation : https://developer.adzuna.com/docs/search
 */
@Component
public class AdzunaClient {

    public static final int PER_PAGE = 20;

    /** Au-delà, les résultats sont rarement pertinents : on limite la pagination. */
    public static final int MAX_PAGE = 25;

    private final RestClient restClient;
    private final String appId;
    private final String appKey;
    private final String country;

    @Autowired
    public AdzunaClient(@Value("${lulu.adzuna.base-url}") String baseUrl,
                        @Value("${lulu.adzuna.app-id:}") String appId,
                        @Value("${lulu.adzuna.app-key:}") String appKey,
                        @Value("${lulu.adzuna.country:fr}") String country) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
    }

    /** Constructeur utilisé par les tests, avec un RestClient simulé. */
    AdzunaClient(RestClient restClient, String appId, String appKey, String country) {
        this.restClient = restClient;
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
    }

    public boolean isConfigured() {
        return appId != null && !appId.isBlank() && appKey != null && !appKey.isBlank();
    }

    /**
     * Recherche des offres contenant exactement l'expression donnée.
     *
     * @param page numéro de page, à partir de 1
     */
    public AdzunaSearchResponse search(String expression, String where, int page) {
        if (!isConfigured()) {
            throw new ApiUnavailableException(
                    "Identifiants Adzuna absents : ajoutez ADZUNA_APP_ID et ADZUNA_APP_KEY dans le fichier .env, "
                            + "puis relancez l'application.");
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/v1/api/jobs/{country}/search/{page}")
                                .queryParam("app_id", appId)
                                .queryParam("app_key", appKey)
                                .queryParam("results_per_page", PER_PAGE)
                                .queryParam("what_phrase", expression)
                                .queryParam("sort_by", "date");
                        if (where != null && !where.isBlank()) {
                            uriBuilder.queryParam("where", where);
                        }
                        return uriBuilder.build(country, page);
                    })
                    .retrieve()
                    .body(AdzunaSearchResponse.class);

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new ApiUnavailableException(
                    "Adzuna refuse les identifiants : vérifiez ADZUNA_APP_ID et ADZUNA_APP_KEY dans .env.", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new ApiUnavailableException(
                    "Limite d'appels Adzuna atteinte : réessayez dans quelques minutes.", e);
        } catch (RestClientException e) {
            throw new ApiUnavailableException(
                    "Adzuna ne répond pas pour le moment. Réessayez dans quelques instants.", e);
        }
    }
}
