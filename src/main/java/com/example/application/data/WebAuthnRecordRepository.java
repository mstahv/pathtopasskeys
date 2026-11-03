package com.example.application.data;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WebAuthnRecordRepository implements PanacheRepository<WebAuthnRecord> {

    public List<WebAuthnRecord> findByUser(User user) {
        return list("user", user);
    }

    public List<WebAuthnRecord> findByUsername(String username) {
        return list("user.username", username);
    }

    public Optional<WebAuthnRecord> findByCredentialId(String credentialId) {
        return find("credentialId", credentialId).firstResultOptional();
    }
}
