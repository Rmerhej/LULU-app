package fr.lulu.app.controller;

import fr.lulu.app.dto.ProfileForm;
import fr.lulu.app.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("profile", ProfileForm.fromEntity(profileService.get()));
        return "profil";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("profile") ProfileForm form, BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profil";
        }
        profileService.save(form);
        redirectAttributes.addFlashAttribute("successMessage", "Profil enregistré.");
        return "redirect:/profil";
    }
}
