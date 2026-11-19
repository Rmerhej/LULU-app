package fr.lulu.app.entity;

import fr.lulu.app.enums.ApplicationStatus;
import fr.lulu.app.enums.OfferSource;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Une offre d'emploi et le suivi de la candidature correspondante.
 */
@Entity
// La contrainte existe aussi dans db/migration/V1 (MySQL) ; la déclarer ici l'applique
// au schéma créé par Hibernate pour les tests (H2), et documente la règle dans le code.
@Table(name = "job_offer", uniqueConstraints = @UniqueConstraint(
        name = "uk_job_offer_source_external", columnNames = {"source", "external_id"}))
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferSource source;

    /** Identifiant chez Adzuna (vide pour une offre ajoutée à la main). */
    @Column(length = 100)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String title;

    private String company;

    private String location;

    @Column(length = 100)
    private String contractType;

    @Column(length = 100)
    private String salary;

    @Column(length = 1000)
    private String url;

    @Column(length = 100000)
    private String description;

    private LocalDateTime publishedAt;

    // ----- Suivi -----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.A_ETUDIER;

    private LocalDate appliedAt;

    @Column(length = 2000)
    private String notes;

    // ----- Analyse IA -----

    private Integer aiScore;

    @Column(length = 2000)
    private String aiStrengths;

    @Column(length = 2000)
    private String aiGaps;

    @Column(length = 2000)
    private String aiAdvice;

    @Column(length = 20000)
    private String coverLetter;

    private LocalDateTime analysedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.A_ETUDIER;
        }
    }

    public Long getId() {
        return id;
    }

    public OfferSource getSource() {
        return source;
    }

    public void setSource(OfferSource source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDate appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getAiScore() {
        return aiScore;
    }

    public void setAiScore(Integer aiScore) {
        this.aiScore = aiScore;
    }

    public String getAiStrengths() {
        return aiStrengths;
    }

    public void setAiStrengths(String aiStrengths) {
        this.aiStrengths = aiStrengths;
    }

    public String getAiGaps() {
        return aiGaps;
    }

    public void setAiGaps(String aiGaps) {
        this.aiGaps = aiGaps;
    }

    public String getAiAdvice() {
        return aiAdvice;
    }

    public void setAiAdvice(String aiAdvice) {
        this.aiAdvice = aiAdvice;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getAnalysedAt() {
        return analysedAt;
    }

    public void setAnalysedAt(LocalDateTime analysedAt) {
        this.analysedAt = analysedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
