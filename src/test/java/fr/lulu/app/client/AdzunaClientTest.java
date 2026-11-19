package fr.lulu.app.client;

import fr.lulu.app.client.dto.AdzunaSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Aucun appel réseau : MockRestServiceServer simule Adzuna et vérifie la requête envoyée.
 */
class AdzunaClientTest {

    private static final String BASE_URL = "https://api.test";

    private static final String JSON = """
            {
              "count": 42,
              "results": [
                {
                  "id": "5123456789",
                  "title": "Assistant <strong>développement produit</strong> H/F",
                  "description": "Au sein d'une maison de luxe...",
                  "redirect_url": "https://www.adzuna.fr/details/5123456789",
                  "created": "2026-11-18T09:30:00Z",
                  "company": { "display_name": "Maison Exemple" },
                  "location": { "display_name": "Paris, Ile-de-France" },
                  "contract_type": "permanent",
                  "contract_time": "full_time",
                  "salary_min": 38000,
                  "salary_max": 42000,
                  "champ_inconnu": "ignoré"
                }
              ]
            }
            """;

    private MockRestServiceServer server;
    private AdzunaClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AdzunaClient(builder.build(), "mon-id", "ma-cle", "fr");
    }

    @Test
    void envoieLaRechercheEtLitLaReponse() {
        server.expect(requestTo(startsWith(BASE_URL + "/v1/api/jobs/fr/search/2")))
                .andExpect(queryContains("what_phrase=développement produit"))
                .andExpect(queryContains("where=Ile-de-France"))
                .andExpect(queryContains("app_id=mon-id"))
                .andExpect(queryContains("results_per_page=20"))
                .andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

        AdzunaSearchResponse response = client.search("développement produit", "Ile-de-France", 2);

        server.verify();
        assertEquals(42, response.count());
        AdzunaSearchResponse.AdzunaJob job = response.safeResults().getFirst();
        assertEquals("5123456789", job.id());
        assertEquals("Maison Exemple", job.company().displayName());
        assertEquals("permanent", job.contractType());
        assertEquals(42000.0, job.salaryMax());
    }

    @Test
    void identifiantsRefusesDonnentUnMessageClair() {
        server.expect(requestTo(startsWith(BASE_URL))).andRespond(withStatus(UNAUTHORIZED));

        ApiUnavailableException e = assertThrows(ApiUnavailableException.class,
                () -> client.search("acheteur mode", null, 1));
        assertTrue(e.getMessage().contains("ADZUNA_APP_ID"));
    }

    @Test
    void sansIdentifiantsAucunAppel() {
        AdzunaClient sansCle = new AdzunaClient(RestClient.create(), "", "", "fr");

        ApiUnavailableException e = assertThrows(ApiUnavailableException.class,
                () -> sansCle.search("acheteur mode", null, 1));
        assertTrue(e.getMessage().contains("absents"));
    }

    /** Vérifie un paramètre de l'URL, quel que soit son encodage. */
    private static RequestMatcher queryContains(String expected) {
        return request -> {
            String query = URLDecoder.decode(request.getURI().getRawQuery(), StandardCharsets.UTF_8);
            assertTrue(List.of(query.split("&")).contains(expected),
                    () -> "Paramètre attendu : " + expected + " — reçu : " + query);
        };
    }
}
