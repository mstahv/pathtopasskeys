package com.example.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;

import java.util.concurrent.CompletableFuture;

/**
 * Java API for the browser side of the WebAuthn ceremonies. Quarkus serves a
 * small helper script, webauthn.js, which calls the browser's WebAuthn API and
 * talks to the server endpoints. We only load it to the page and call it.
 */
public final class WebAuthnJs {

    private WebAuthnJs() {
    }

    /**
     * Starts a passkey login against the built-in Quarkus endpoint. On success
     * the browser is redirected into the app, on failure to {@code /login?error}.
     */
    public static void authenticate() {
        page().executeJs("""
                new WebAuthn().login()
                    .then(() => window.location.href = "/")
                    .catch(e => { console.error(e); window.location.href = "/login?error"; });
                """);
    }

    /**
     * Creates a new passkey and stores it on the server, using the built-in
     * Quarkus endpoint. For a new username, this creates the account too (see
     * {@link PasskeyUserProvider#store}). The user is signed in afterwards.
     *
     * @return a future that completes when the passkey is stored, or fails if the
     * browser or server rejects the registration
     */
    public static CompletableFuture<Void> register(String username) {
        return page().executeJs("return new WebAuthn().register({username: $0}).then(() => {});", username)
                .toCompletableFuture(Void.class);
    }

    private static Page page() {
        Page page = UI.getCurrent().getPage();
        // Loaded only once per page, and before the script calls above are run
        page.addJavaScript("context://q/webauthn/webauthn.js");
        return page;
    }
}
