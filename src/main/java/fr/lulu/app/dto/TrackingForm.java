package fr.lulu.app.dto;

import fr.lulu.app.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Suivi d'une candidature : étape, date d'envoi, notes.
 */
public class TrackingForm {

    @NotNull(message = "Choisissez une étape")
    private ApplicationStatus status;

    // Format du champ <input type="date"> : 2026-11-19
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appliedAt;

    @Size(max = 2000, message = "2000 caractères maximum")
    private String notes;

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
}
