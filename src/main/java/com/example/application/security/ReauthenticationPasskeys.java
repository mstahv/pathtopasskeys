package com.example.application.security;

import com.example.application.data.WebAuthnRecordRepository;
import in.virit.passkeyreauth.PasskeyReauthentication;
import in.virit.passkeyreauth.PasskeyStore;
import in.virit.passkeyreauth.StoredPasskey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Re-authentication with a passkey before sensitive actions (see
 * {@link com.example.application.views.SensitiveActionView}). The library does
 * the WebAuthn part over Vaadin's own communication, independent of the Quarkus
 * WebAuthn extension. It only needs to know the passkeys of the current user,
 * and the same relying party settings as the login.
 */
@ApplicationScoped
public class ReauthenticationPasskeys implements PasskeyStore {

    private final AuthenticatedUser authenticatedUser;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public ReauthenticationPasskeys(AuthenticatedUser authenticatedUser,
                                    WebAuthnRecordRepository webAuthnRecordRepository) {
        this.authenticatedUser = authenticatedUser;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    /**
     * Uses the relying party id and origins of the WebAuthn extension. If they are
     * not configured (like here in dev mode and tests), both the extension and
     * the library deduce them from the request.
     */
    @Produces
    @Singleton
    PasskeyReauthentication passkeyReauthentication(
            @ConfigProperty(name = "quarkus.webauthn.relying-party.id") Optional<String> rpId,
            @ConfigProperty(name = "quarkus.webauthn.origins") Optional<List<String>> origins) {
        var reauthentication = new PasskeyReauthentication(this);
        rpId.ifPresent(reauthentication::withRelyingPartyId);
        origins.ifPresent(o -> reauthentication.withAllowedOrigins(o.toArray(String[]::new)));
        return reauthentication;
    }

    @Override
    public List<StoredPasskey> passkeysOfCurrentUser() {
        // Quarkus stores the public key X.509 encoded, with the algorithm separately
        return authenticatedUser.getPasskeys().stream()
                .map(p -> StoredPasskey.fromX509(p.credentialId, p.publicKey, p.publicKeyAlgorithm, p.counter))
                .toList();
    }

    @Override
    @Transactional
    public void signatureCountUpdated(StoredPasskey passkey, long newSignatureCount) {
        String credentialId = Base64.getUrlEncoder().withoutPadding().encodeToString(passkey.credentialId());
        webAuthnRecordRepository.findByCredentialId(credentialId)
                .ifPresent(record -> record.counter = newSignatureCount);
    }
}
