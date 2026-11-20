// Application installable (PWA) : enregistrement du service worker + bannière "Installer"
if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
        navigator.serviceWorker.register("/sw.js").catch(() => { /* sans service worker, l'app marche quand même */ });
    });
}

// Android / Chrome / Edge : le navigateur propose l'installation, on l'affiche discrètement
let installPrompt = null;
window.addEventListener("beforeinstallprompt", event => {
    event.preventDefault();
    installPrompt = event;
    const banner = document.getElementById("lulu-install");
    if (!banner || localStorageGet("lulu-install-ferme")) {
        return;
    }
    banner.hidden = false;
    banner.querySelector(".lulu-install-ok").addEventListener("click", async () => {
        banner.hidden = true;
        installPrompt.prompt();
        installPrompt = null;
    });
    banner.querySelector(".lulu-install-close").addEventListener("click", () => {
        banner.hidden = true;
        localStorageSet("lulu-install-ferme", "1");
    });
});

function localStorageGet(key) {
    try { return localStorage.getItem(key); } catch (e) { return null; }
}

function localStorageSet(key, value) {
    try { localStorage.setItem(key, value); } catch (e) { /* navigation privée */ }
}
