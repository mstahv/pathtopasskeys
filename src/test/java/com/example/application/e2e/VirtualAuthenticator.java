package com.example.application.e2e;

import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;

/**
 * A software "security key" for Chromium, via the Chrome DevTools Protocol
 * WebAuthn domain. Once attached, navigator.credentials.create/get calls on the
 * page are answered by this authenticator without any user interaction.
 * <p>
 * The authenticator lives as long as the page, so a test can register a passkey,
 * log out and log back in with it.
 */
public class VirtualAuthenticator {

    private final CDPSession cdp;
    private final String authenticatorId;

    private VirtualAuthenticator(Page page) {
        cdp = page.context().newCDPSession(page);
        cdp.send("WebAuthn.enable");

        var options = new JsonObject();
        options.addProperty("protocol", "ctap2");
        options.addProperty("transport", "internal");
        options.addProperty("hasResidentKey", true); // discoverable credential == passkey
        options.addProperty("hasUserVerification", true);
        options.addProperty("isUserVerified", true);
        options.addProperty("automaticPresenceSimulation", true);
        var params = new JsonObject();
        params.add("options", options);

        authenticatorId = cdp.send("WebAuthn.addVirtualAuthenticator", params)
                .get("authenticatorId").getAsString();
    }

    public static VirtualAuthenticator attachTo(Page page) {
        return new VirtualAuthenticator(page);
    }

    /**
     * @return the number of credentials (passkeys) stored in this authenticator
     */
    public int credentialCount() {
        var params = new JsonObject();
        params.addProperty("authenticatorId", authenticatorId);
        return cdp.send("WebAuthn.getCredentials", params)
                .getAsJsonArray("credentials").size();
    }
}
