document.addEventListener("DOMContentLoaded", () => {

    // Recherche : choisir une expression toute faite remplit le champ "Expression recherchée"
    const preset = document.getElementById("preset");
    const query = document.getElementById("q");
    if (preset && query) {
        preset.addEventListener("change", () => {
            if (preset.value) {
                query.value = preset.value;
                query.focus();
            }
        });
    }

    // Analyse IA : bouton désactivé pendant l'appel (quelques secondes), pour éviter les doubles clics
    document.querySelectorAll("form.js-busy").forEach(form => {
        form.addEventListener("submit", () => {
            const button = form.querySelector("button[type=submit]");
            if (button) {
                button.disabled = true;
                button.textContent = button.dataset.busy || "Patientez…";
            }
        });
    });

    // Confirmation avant suppression
    document.querySelectorAll("form.js-confirm").forEach(form => {
        form.addEventListener("submit", event => {
            if (!confirm(form.dataset.confirm || "Confirmer ?")) {
                event.preventDefault();
            }
        });
    });

    // Copier la lettre dans le presse-papiers
    const copy = document.getElementById("copy-letter");
    const letter = document.getElementById("letter");
    if (copy && letter) {
        copy.addEventListener("click", async () => {
            try {
                await navigator.clipboard.writeText(letter.value);
                copy.textContent = "Lettre copiée";
            } catch (e) {
                copy.textContent = "Copie impossible";
            }
            setTimeout(() => copy.textContent = "Copier la lettre", 2000);
        });
    }
});
