package fr.lulu.app.controller;

import fr.lulu.app.client.ApiUnavailableException;
import fr.lulu.app.enums.JobFamily;
import fr.lulu.app.service.JobSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Recherche d'offres chez Adzuna et import dans "Mes offres".
 */
@Controller
@RequestMapping("/recherche")
public class SearchController {

    private static final int MAX_EXPRESSION = 100;

    private final JobSearchService searchService;
    private final String defaultWhere;

    public SearchController(JobSearchService searchService,
                            @Value("${lulu.adzuna.where:Ile-de-France}") String defaultWhere) {
        this.searchService = searchService;
        this.defaultWhere = defaultWhere;
    }

    @ModelAttribute("families")
    public JobFamily[] families() {
        return JobFamily.values();
    }

    @GetMapping
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String where,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {

        String expression = clean(q);
        String location = where == null ? defaultWhere : clean(where);
        model.addAttribute("q", expression);
        model.addAttribute("where", location);

        if (expression != null) {
            try {
                model.addAttribute("result", searchService.search(expression, location, page));
            } catch (ApiUnavailableException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return "recherche";
    }

    @PostMapping
    public String importSelected(@RequestParam String q,
                                 @RequestParam(required = false) String where,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) List<String> ids,
                                 RedirectAttributes redirectAttributes) {
        String expression = clean(q);
        String location = clean(where);
        try {
            int imported = searchService.importSelected(expression, location, page, ids);
            if (ids == null || ids.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cochez au moins une offre.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        imported + " offre(s) ajoutée(s) à « Mes offres ».");
            }
        } catch (ApiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Retour sur la même page de résultats (Spring encode les paramètres)
        redirectAttributes.addAttribute("q", expression);
        redirectAttributes.addAttribute("where", location == null ? "" : location);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/recherche";
    }

    /** Texte saisi dans l'adresse : nettoyé et borné. */
    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        return text.length() <= MAX_EXPRESSION ? text : text.substring(0, MAX_EXPRESSION);
    }
}
