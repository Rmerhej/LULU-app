/*
 * Service worker de LULU.
 * - Mise en page (CSS, JS, icônes, polices) : servie depuis le cache, l'app s'ouvre vite.
 * - Pages (offres, profil…) : TOUJOURS demandées au serveur, jamais mises en cache
 *   (données personnelles : rien ne reste sur le téléphone).
 * - Sans réseau : page "Pas de connexion".
 * Changer VERSION à chaque modification de ce fichier ou des fichiers mis en cache.
 */
const VERSION = "lulu-v1";
const OFFLINE_URL = "/offline.html";
const PRECACHE = [
    OFFLINE_URL,
    "/css/lulu.css",
    "/js/app.js",
    "/js/pwa.js",
    "/icons/icon-192.png",
    "/manifest.json"
];

self.addEventListener("install", event => {
    event.waitUntil(caches.open(VERSION).then(cache => cache.addAll(PRECACHE)));
    self.skipWaiting();
});

self.addEventListener("activate", event => {
    // Supprime les caches des versions précédentes
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(keys.filter(key => key !== VERSION).map(key => caches.delete(key))))
            .then(() => self.clients.claim())
    );
});

function isStatic(url) {
    const sameOrigin = url.origin === self.location.origin
        && /^\/(css|js|icons)\//.test(url.pathname);
    const cdn = ["fonts.googleapis.com", "fonts.gstatic.com", "cdn.jsdelivr.net"].includes(url.hostname);
    return sameOrigin || cdn;
}

self.addEventListener("fetch", event => {
    const request = event.request;
    if (request.method !== "GET") {
        return; // formulaires : jamais interceptés
    }

    // Navigation (ouverture d'une page) : réseau uniquement, page hors connexion en secours
    if (request.mode === "navigate") {
        event.respondWith(fetch(request).catch(() => caches.match(OFFLINE_URL)));
        return;
    }

    // Mise en page : cache d'abord, mise à jour en arrière-plan
    const url = new URL(request.url);
    if (isStatic(url)) {
        event.respondWith(
            caches.open(VERSION).then(cache =>
                cache.match(request).then(cached => {
                    const network = fetch(request).then(response => {
                        if (response.ok || response.type === "opaque") {
                            cache.put(request, response.clone());
                        }
                        return response;
                    }).catch(() => cached);
                    return cached || network;
                })
            )
        );
    }
});
