// Mots de passe : un bouton « œil » pour afficher ou masquer ce qu'on tape
document.addEventListener("DOMContentLoaded", () => {
    const OEIL = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg>';
    const OEIL_BARRE = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 3l18 18M10.6 5.1A10.9 10.9 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.2 4.2M6.6 6.6C3.8 8.4 2 12 2 12s3.5 7 10 7c1.8 0 3.4-.5 4.8-1.3M9.9 9.9a3 3 0 0 0 4.2 4.2"/></svg>';

    document.querySelectorAll('input[type="password"]').forEach(input => {
        const conteneur = document.createElement("div");
        conteneur.className = "lulu-password";
        input.parentNode.insertBefore(conteneur, input);
        conteneur.appendChild(input);

        const bouton = document.createElement("button");
        bouton.type = "button";
        bouton.className = "lulu-password-toggle";
        conteneur.appendChild(bouton);

        const afficher = visible => {
            input.type = visible ? "text" : "password";
            bouton.setAttribute("aria-pressed", String(visible));
            bouton.setAttribute("aria-label", visible ? "Masquer le mot de passe" : "Afficher le mot de passe");
            bouton.innerHTML = visible ? OEIL_BARRE : OEIL;
        };
        afficher(false);
        bouton.addEventListener("click", () => {
            afficher(input.type === "password");
            input.focus();
        });
        // Par sécurité, on remasque le mot de passe à l'envoi du formulaire
        if (input.form) {
            input.form.addEventListener("submit", () => { input.type = "password"; });
        }
    });
});
