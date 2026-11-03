package com.example.application.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A passkey (WebAuthn credential record) of a {@link User}, persisted with JPA.
 * <p>
 * The columns mirror the {@code user_credentials} table of Spring Security's
 * {@link org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository}.
 * If plain JDBC fits your architecture, you can use that ready-made
 * implementation instead of this entity and
 * {@link com.example.application.security.WebAuthnUserCredentialRepositoryImpl}.
 */
@Entity
public class WebAuthnRecord extends AbstractEntity {

    @ManyToOne
    public User user;

    @Column(columnDefinition = "bytea", unique = true)
    public byte[] credentialId;

    /**
     * The WebAuthn id of the user ({@link User#getWebAuthnId()}), as reported by the authenticator.
     */
    @Column(columnDefinition = "bytea")
    public byte[] userEntityUserId;

    @Column(columnDefinition = "bytea")
    public byte[] publicKey;

    public long signatureCount;

    public boolean uvInitialized;

    public boolean backupEligible;

    public boolean backupState;

    /**
     * Comma separated {@link AuthenticatorTransport} values, e.g. "internal,hybrid"
     */
    public String transports;

    @Column(columnDefinition = "bytea")
    public byte[] attestationObject;

    @Column(columnDefinition = "bytea")
    public byte[] attestationClientDataJSON;

    public Instant created;

    public Instant lastUsed;

    public String label;

    /**
     * Copies the state of the given credential record (from Spring Security) to this entity.
     */
    public void update(CredentialRecord record) {
        credentialId = record.getCredentialId().getBytes();
        userEntityUserId = record.getUserEntityUserId().getBytes();
        publicKey = record.getPublicKey().getBytes();
        signatureCount = record.getSignatureCount();
        uvInitialized = record.isUvInitialized();
        backupEligible = record.isBackupEligible();
        backupState = record.isBackupState();
        transports = record.getTransports().stream()
                .map(AuthenticatorTransport::getValue)
                .collect(Collectors.joining(","));
        attestationObject = bytesOrNull(record.getAttestationObject());
        attestationClientDataJSON = bytesOrNull(record.getAttestationClientDataJSON());
        created = record.getCreated();
        lastUsed = record.getLastUsed();
        label = record.getLabel();
    }

    public CredentialRecord asCredentialRecord() {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(new Bytes(credentialId))
                .userEntityUserId(new Bytes(userEntityUserId))
                .publicKey(new ImmutablePublicKeyCose(publicKey))
                .signatureCount(signatureCount)
                .uvInitialized(uvInitialized)
                .backupEligible(backupEligible)
                .backupState(backupState)
                .transports(Arrays.stream(transports.split(","))
                        .filter(t -> !t.isBlank())
                        .map(AuthenticatorTransport::valueOf)
                        .collect(Collectors.toSet()))
                .attestationObject(attestationObject == null ? null : new Bytes(attestationObject))
                .attestationClientDataJSON(attestationClientDataJSON == null ? null : new Bytes(attestationClientDataJSON))
                .created(created)
                .lastUsed(lastUsed)
                .label(label)
                .build();
    }

    private static byte[] bytesOrNull(Bytes bytes) {
        return bytes == null ? null : bytes.getBytes();
    }
}
