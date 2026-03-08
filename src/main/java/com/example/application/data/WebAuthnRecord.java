package com.example.application.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.springframework.security.web.webauthn.api.*;
import tools.jackson.core.Base64Variants;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

import java.time.Instant;
import java.util.Base64;
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


    private static JsonMapper jm = JsonMapper.builder()
            .defaultBase64Variant(Base64Variants.MODIFIED_FOR_URL)
            .build();

    // Helpers to map the entity to/from WebAuthn4J/SpringSecurity API

    public static WebAuthnRecord of(CredentialRecord credentialRecord) {
        WebAuthnRecord webAuthnRecord = new WebAuthnRecord();
        webAuthnRecord.credentialId = credentialRecord.getCredentialId().getBytes();
        webAuthnRecord.userEntityUserId = credentialRecord.getUserEntityUserId().getBytes();
        webAuthnRecord.updateJson(credentialRecord);
        return webAuthnRecord;
    }

    public CredentialRecord asCredentialRecord() {
        // ideally this would implement CredentialRecord, but not tempting looking interface for JPA
        JsonNode jsonNode = jm.readTree(recordJson);

        String publickey = jsonNode.get("publicKey").get("bytes").asString();
        String attestationObject = jsonNode.get("attestationObject").get("bytes").asString();
        String attestationClientDataJSON = jsonNode.get("attestationClientDataJSON").get("bytes").asString();
        String created = jsonNode.get("created").asString();
        String lastUsed = jsonNode.get("lastUsed").asString();
        ArrayNode jsonNode1 = (ArrayNode) jsonNode.get("transports");
        var transports = new HashSet<AuthenticatorTransport>();
        for (int i = 0; i < jsonNode1.size(); i++) {
            transports.add(AuthenticatorTransport.valueOf(jsonNode1.get(i).get("value").asString()));
        }

        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(new Bytes(credentialId))
                .userEntityUserId(new Bytes(userEntityUserId))
                .signatureCount(jsonNode.get("signatureCount").asInt())
                .attestationObject(Bytes.fromBase64(attestationObject))
                .backupEligible(jsonNode.get("backupEligible").asBoolean())
                .attestationClientDataJSON(Bytes.fromBase64(attestationClientDataJSON))
                .backupState(jsonNode.get("backupState").asBoolean())
                .publicKey(() -> Bytes.fromBase64(publickey).getBytes())
                .transports(transports)
                .lastUsed(Instant.parse(lastUsed))
                .created(Instant.parse(created))
                .label(jsonNode.get("label").asString())
                .uvInitialized(jsonNode.get("uvInitialized").asBoolean())
                .build();
    }

    public void updateJson(CredentialRecord credentialRecord) {
        recordJson = jm.writeValueAsString(credentialRecord);
    }
}
