package com.example.application.security;

import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Tells Quarkus how to find the passkeys (WebAuthn credential records) when a
 * user logs in. The records are stored with JPA, see {@link WebAuthnRecord}.
 * <p>
 * The API is reactive, but JPA is blocking, so {@code @Blocking} makes Quarkus
 * call these methods on a worker thread. The values are computed eagerly within
 * the methods, so that they run inside the transaction.
 * <p>
 * New passkeys are stored by {@link PasskeyRegistration}, which is why
 * {@link #store} is not implemented: Quarkus' own registration endpoint (the
 * only caller) is disabled.
 */
@ApplicationScoped
@Blocking
public class PasskeyUserProvider implements WebAuthnUserProvider {

    private final WebAuthnRecordRepository records;

    public PasskeyUserProvider(WebAuthnRecordRepository records) {
        this.records = records;
    }

    @Override
    @Transactional
    public Uni<List<WebAuthnCredentialRecord>> findByUsername(String username) {
        return Uni.createFrom().item(records.findByUsername(username).stream()
                .map(WebAuthnRecord::asCredentialRecord)
                .toList());
    }

    @Override
    @Transactional
    public Uni<WebAuthnCredentialRecord> findByCredentialId(String credentialId) {
        return records.findByCredentialId(credentialId)
                .map(record -> Uni.createFrom().item(record.asCredentialRecord()))
                .orElseGet(() -> Uni.createFrom().failure(new IllegalArgumentException("Unknown passkey")));
    }

    /**
     * Called after each login. Authenticators increase the counter on each use,
     * which helps to detect cloned authenticators.
     */
    @Override
    @Transactional
    public Uni<Void> update(String credentialId, long counter) {
        records.findByCredentialId(credentialId).ifPresent(record -> record.counter = counter);
        return Uni.createFrom().voidItem();
    }
}
