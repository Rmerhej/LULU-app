package fr.lulu.app.dto;

import fr.lulu.app.entity.CandidateProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulaire du profil. Aucune coordonnée personnelle n'est demandée.
 */
public class ProfileForm {

    @Size(max = 120, message = "120 caractères maximum")
    private String fullName;

    @NotBlank(message = "Indiquez le poste recherché")
    @Size(max = 200, message = "200 caractères maximum")
    private String headline;

    @Size(max = 2000, message = "2000 caractères maximum")
    private String summary;

    @NotBlank(message = "Collez au moins les expériences du CV")
    @Size(max = 20000, message = "20 000 caractères maximum")
    private String experience;

    @Size(max = 1000, message = "1000 caractères maximum")
    private String skills;

    @Size(max = 500, message = "500 caractères maximum")
    private String languages;

    public static ProfileForm fromEntity(CandidateProfile profile) {
        ProfileForm form = new ProfileForm();
        form.fullName = profile.getFullName();
        form.headline = profile.getHeadline();
        form.summary = profile.getSummary();
        form.experience = profile.getExperience();
        form.skills = profile.getSkills();
        form.languages = profile.getLanguages();
        return form;
    }

    public void applyTo(CandidateProfile profile) {
        profile.setFullName(strip(fullName));
        profile.setHeadline(strip(headline));
        profile.setSummary(strip(summary));
        profile.setExperience(strip(experience));
        profile.setSkills(strip(skills));
        profile.setLanguages(strip(languages));
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }
}
