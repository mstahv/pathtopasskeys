package com.example.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Java API for the browser side of the WebAuthn ceremonies (webauthn_vf.js).
 * The script talks directly to the endpoints Spring Security provides
 * ({@code /webauthn/**} and {@code /login/webauthn}), so the server side logic is
 * all in Spring Security. We only pass in the CSRF token, so that the
 * endpoints don't need to be excluded from CSRF protection.
 */
public final class WebAuthnJs {

    private static final String SCRIPT = loadScript();

    private WebAuthnJs() {
    }

    /**
     * Starts a passkey login. On success the browser is redirected into the app,
     * on failure to {@code /login?error}.
     */
    public static void authenticate() {
        CsrfToken csrf = csrfToken();
        ui().getPage().executeJs(SCRIPT + "window.webauthnVf.authenticateOrError({[$0]: $1});",
                csrf.getHeaderName(), csrf.getToken());
    }

    /**
     * Creates a new passkey for the current user and stores it on the server.
     *
     * @param label the name of the passkey, shown to the user when listing passkeys
     * @return a future that completes when the passkey is stored, or fails if the
     * browser or server rejects the registration
     */
    public static CompletableFuture<Void> register(String label) {
        CsrfToken csrf = csrfToken();
        return ui().getPage().executeJs(SCRIPT + "return window.webauthnVf.register({[$0]: $1}, $2);",
                        csrf.getHeaderName(), csrf.getToken(), label)
                .toCompletableFuture(Void.class);
    }

    private static UI ui() {
        return UI.getCurrent();
    }

    private static CsrfToken csrfToken() {
        // Spring Security's CsrfFilter makes the token available for every request,
        // also for Vaadin's own requests that are excluded from the CSRF check
        return (CsrfToken) VaadinServletRequest.getCurrent().getAttribute(CsrfToken.class.getName());
    }

    private static String loadScript() {
        try (InputStream in = WebAuthnJs.class.getResourceAsStream("/webauthn_vf.js")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
