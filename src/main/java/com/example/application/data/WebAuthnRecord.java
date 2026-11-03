package com.example.application.data;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord.RequiredPersistedData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.Instant;
import java.util.UUID;

/**
 * A passkey (WebAuthn credential record) of a {@link User}, persisted with JPA.
 * <p>
 * Quarkus needs only the few fields of {@link RequiredPersistedData} to verify
 * logins later on. The label and the creation time are just for the user, to
 * tell their passkeys apart in the Profile view.
 */
@Entity
public class WebAuthnRecord extends AbstractEntity {

    @ManyToOne(optional = false)
    public User user;

    @Column(unique = true, nullable = false)
    public String credentialId;

    public byte[] publicKey;

    public long publicKeyAlgorithm;

    public long counter;

    public UUID aaguid;

    public String label;

    public Instant created;

    public static WebAuthnRecord of(User user, WebAuthnCredentialRecord credential, String label) {
        var data = credential.getRequiredPersistedData();
        var record = new WebAuthnRecord();
        record.user = user;
        record.credentialId = data.credentialId();
        record.publicKey = data.publicKey();
        record.publicKeyAlgorithm = data.publicKeyAlgorithm();
        record.counter = data.counter();
        record.aaguid = data.aaguid();
        record.label = label;
        record.created = Instant.now();
        return record;
    }

    public WebAuthnCredentialRecord asCredentialRecord() {
        return WebAuthnCredentialRecord.fromRequiredPersistedData(new RequiredPersistedData(
                user.getUsername(), credentialId, aaguid, publicKey, publicKeyAlgorithm, counter));
    }
}
