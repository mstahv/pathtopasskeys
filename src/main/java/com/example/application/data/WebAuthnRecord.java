package com.example.application.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.jackson.WebauthnJackson2Module;

import java.time.Instant;
import java.util.HashSet;

@Entity
public class WebAuthnRecord extends AbstractEntity {

    public WebAuthnRecord() {
    }

    @ManyToOne
    public User user;

    @Column(columnDefinition="bytea")
    public byte[] credentialId;

    // TODO this could be a foreign key to the user table instead of a byte user
    @Column(columnDefinition="bytea")
    public byte[] userEntityUserId;

    @Column(columnDefinition="TEXT")
    public String recordJson;


    // Helpers to map the entity to/from WebAuthn4J/SpringSecurity API
    private static ObjectMapper om = Jackson2ObjectMapperBuilder.json().modules(new WebauthnJackson2Module(), new JavaTimeModule()).build();

    public static WebAuthnRecord of(CredentialRecord credentialRecord) {
        WebAuthnRecord webAuthnRecord = new WebAuthnRecord();
        webAuthnRecord.credentialId = credentialRecord.getCredentialId().getBytes();
        webAuthnRecord.userEntityUserId = credentialRecord.getUserEntityUserId().getBytes();
        webAuthnRecord.updateJson(credentialRecord);
        return webAuthnRecord;
    }

    public CredentialRecord asCredentialRecord() {
        // ideally this would implement CredentialRecord, but not tempting looking interface for JPA
        try {
            JsonNode jsonNode = om.readTree(recordJson);

        long created = jsonNode.get("created").asLong();
        long lastUsed = jsonNode.get("lastUsed").asLong();
        ArrayNode jsonNode1 = (ArrayNode) jsonNode.get("transports");
        var transports = new HashSet<AuthenticatorTransport>();
        for (int i = 0; i < jsonNode1.size(); i++) {
            transports.add(AuthenticatorTransport.valueOf(jsonNode1.get(i).asText()));
        }

        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(new Bytes(credentialId))
                .userEntityUserId(new Bytes(userEntityUserId))
                .signatureCount(jsonNode.get("signatureCount").asInt())
                .attestationObject(Bytes.fromBase64(jsonNode.get("attestationObject").asText()))
                .backupEligible(jsonNode.get("backupEligible").asBoolean())
                .attestationClientDataJSON(Bytes.fromBase64(jsonNode.get("attestationClientDataJSON").asText()))
                .backupState(jsonNode.get("backupState").asBoolean())
                .publicKey(() -> Bytes.fromBase64(jsonNode.get("publicKey").asText()).getBytes())
                .transports(transports)
                .lastUsed(Instant.ofEpochMilli(lastUsed))
                .created(Instant.ofEpochMilli(created))
                .label(jsonNode.get("label").asText())
                .uvInitialized(jsonNode.get("uvInitialized").asBoolean())
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateJson(CredentialRecord credentialRecord) {
        try {
            recordJson = om.writeValueAsString(credentialRecord);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
