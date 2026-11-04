package com.example.application.security;

import com.example.application.data.WebAuthnRecordRepository;
import in.virit.passkeyreauth.PasskeyReauthentication;
import in.virit.passkeyreauth.PasskeyStore;
import in.virit.passkeyreauth.StoredPasskey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

/**
 * Re-authentication with a passkey before sensitive actions (see
 * {@link com.example.application.views.SensitiveActionView}). The library does
 * the WebAuthn part over Vaadin's own communication, independent of Spring
 * Security. It only needs to know the passkeys of the current user, and the same
 * relying party settings as the login.
 */
@Configuration
public class ReauthenticationConfiguration {

    @Bean
    PasskeyReauthentication passkeyReauthentication(AuthenticatedUser authenticatedUser,
            WebAuthnRecordRepository webAuthnRecordRepository,
            @Value("${webauthn.id}") String webauthnId,
            @Value("${webauthn.origin}") String webauthnOrigin) {
        return new PasskeyReauthentication(new PasskeyStore() {
            @Override
            public List<StoredPasskey> passkeysOfCurrentUser() {
                // Spring Security stores the public key COSE encoded, like the library wants it
                return authenticatedUser.getPasskeys().stream()
                        .map(p -> new StoredPasskey(p.credentialId, p.publicKey, p.signatureCount))
                        .toList();
            }

            @Override
            public void signatureCountUpdated(StoredPasskey passkey, long newSignatureCount) {
                webAuthnRecordRepository.findByCredentialId(passkey.credentialId()).ifPresent(record -> {
                    record.signatureCount = newSignatureCount;
                    record.lastUsed = Instant.now();
                    webAuthnRecordRepository.save(record);
                });
            }
        })
                .withRelyingPartyId(webauthnId)
                .withAllowedOrigins(webauthnOrigin);
    }
}
