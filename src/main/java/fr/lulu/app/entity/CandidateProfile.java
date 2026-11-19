package fr.lulu.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Profil de la candidate. Volontairement sans coordonnées (téléphone, e-mail, adresse) :
 * ces informations sont envoyées à l'IA pour analyser les offres.
 */
@Entity
@Table(name = "candidate_profile")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Utilisé uniquement pour signer les lettres (jamais envoyé à l'IA). */
    @Column(length = 120)
    private String fullName;

    @Column(length = 200)
    private String headline;

    @Column(length = 2000)
    private String summary;

    @Column(length = 20000)
    private String experience;

    @Column(length = 1000)
    private String skills;

    @Column(length = 500)
    private String languages;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Vrai si le profil contient assez d'informations pour analyser une offre. */
    public boolean isComplete() {
        return notBlank(headline) && notBlank(experience);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
