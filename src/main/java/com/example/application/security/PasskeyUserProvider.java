package com.example.application.security;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Tells Quarkus how to find and store the passkeys (WebAuthn credential
 * records), which we keep with JPA, see {@link WebAuthnRecord}. With this,
 * the built-in endpoints of Quarkus do the rest: login, logout and registration.
 * <p>
 * The API is reactive, but JPA is blocking, so {@code @Blocking} makes Quarkus
 * call these methods on a worker thread. The values are computed eagerly within
 * the methods, so that they run inside the transaction.
 */
@ApplicationScoped
@Blocking
public class PasskeyUserProvider implements WebAuthnUserProvider {

    private final UserRepository users;
    private final WebAuthnRecordRepository records;

    public PasskeyUserProvider(UserRepository users, WebAuthnRecordRepository records) {
        this.users = users;
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
     * Called by the built-in registration endpoint with a new, verified, passkey.
     * A new username means a sign-up: anybody can create an account in this
     * demo. Adding passkeys to existing users is guarded by {@link PasskeyRegistrationGuard}.
     */
    @Override
    @Transactional
    public Uni<Void> store(WebAuthnCredentialRecord credential) {
        User user = users.findByUsername(credential.getUsername()).orElseGet(() -> {
            var newUser = new User();
            newUser.setUsername(credential.getUsername());
            newUser.setName(credential.getUsername());
            newUser.setRoles(Set.of(Role.USER));
            users.persist(newUser);
            return newUser;
        });
        String label = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        records.persist(WebAuthnRecord.of(user, credential, label));
        return Uni.createFrom().voidItem();
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
