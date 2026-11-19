package fr.lulu.app.controller;

import fr.lulu.app.ai.AiUnavailableException;
import fr.lulu.app.dto.ManualOfferForm;
import fr.lulu.app.dto.TrackingForm;
import fr.lulu.app.entity.JobOffer;
import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/offres")
public class OfferController {

    private static final int MAX_LONG_TEXT = 30000;

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @ModelAttribute("statuses")
    public ApplicationStatus[] statuses() {
        return ApplicationStatus.values();
    }

    // ----- Liste -----

    @GetMapping
    public String list(@RequestParam(required = false) ApplicationStatus etape, Model model) {
        model.addAttribute("offers", offerService.list(etape));
        model.addAttribute("etape", etape);
        return "offres/list";
    }

    // ----- Ajout manuel -----

    @GetMapping("/nouvelle")
    public String newOffer(Model model) {
        model.addAttribute("offer", new ManualOfferForm());
        return "offres/nouvelle";
    }

    @PostMapping("/nouvelle")
    public String addOffer(@Valid @ModelAttribute("offer") ManualOfferForm form, BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "offres/nouvelle";
        }
        OfferService.ManualAddResult added = offerService.addManual(form);
        if (added.warning() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", added.warning());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Offre ajoutée.");
        }
        return "redirect:/offres/" + added.offer().getId();
    }

    // ----- Fiche -----

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobOffer offer = offerService.get(id);
        model.addAttribute("offer", offer);
        if (!model.containsAttribute("tracking")) {
            TrackingForm tracking = new TrackingForm();
            tracking.setStatus(offer.getStatus());
            tracking.setAppliedAt(offer.getAppliedAt());
            tracking.setNotes(offer.getNotes());
            model.addAttribute("tracking", tracking);
        }
        return "offres/detail";
    }

    @PostMapping("/{id}/analyser")
    public String analyze(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            offerService.analyze(id);
            redirectAttributes.addFlashAttribute("successMessage", "Analyse terminée. Relisez la lettre avant de l'envoyer.");
        } catch (AiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/offres/" + id;
    }

    @PostMapping("/{id}/suivi")
    public String track(@PathVariable Long id, @Valid @ModelAttribute("tracking") TrackingForm form,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("offer", offerService.get(id));
            return "offres/detail";
        }
        offerService.track(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Suivi mis à jour.");
        return "redirect:/offres/" + id;
    }

    @PostMapping("/{id}/lettre")
    public String saveLetter(@PathVariable Long id, @RequestParam String letter,
                             RedirectAttributes redirectAttributes) {
        if (letter.length() > MAX_LONG_TEXT) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lettre trop longue.");
        } else {
            offerService.saveLetter(id, letter);
            redirectAttributes.addFlashAttribute("successMessage", "Lettre enregistrée.");
        }
        return "redirect:/offres/" + id;
    }

    @PostMapping("/{id}/description")
    public String saveDescription(@PathVariable Long id, @RequestParam String description,
                                  RedirectAttributes redirectAttributes) {
        if (description.length() > MAX_LONG_TEXT) {
            redirectAttributes.addFlashAttribute("errorMessage", "Texte trop long (30 000 caractères maximum).");
        } else {
            offerService.saveDescription(id, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Description mise à jour. Relancez l'analyse pour en tenir compte.");
        }
        return "redirect:/offres/" + id;
    }

    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        offerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Offre supprimée.");
        return "redirect:/offres";
    }
}
