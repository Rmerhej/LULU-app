package fr.lulu.app.controller;

import fr.lulu.app.repository.JobOfferRepository;
import fr.lulu.app.service.DashboardService;
import fr.lulu.app.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final ProfileService profileService;
    private final JobOfferRepository offerRepository;

    public HomeController(DashboardService dashboardService, ProfileService profileService,
                          JobOfferRepository offerRepository) {
        this.dashboardService = dashboardService;
        this.profileService = profileService;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stats", dashboardService.compute());
        var profile = profileService.get();
        model.addAttribute("profileComplete", profile.isComplete());
        model.addAttribute("firstName", firstName(profile.getFullName()));
        model.addAttribute("latest", offerRepository.findTop5ByOrderByCreatedAtDesc());
        return "index";
    }

    /** Le prénom (premier mot du nom), pour saluer l'utilisatrice. */
    static String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        return fullName.strip().split("\\s+")[0];
    }
}
