package com.example.application.security;

import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This repository persists credential records (aka passkeys) to the database.
 * Spring Security comes with (and automatically configures!!) a default implementation
 * that stores the credentials in memory, but you will never want to use that as:
 *
 *  * passkeys only work on single node deployments and
 *  * passkeys are lost if you restart the server
 *
 */
@Service
public class WebAuthnUserCredentialRepositoryImpl implements UserCredentialRepository {
    private final UserRepository userRepository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public WebAuthnUserCredentialRepositoryImpl(UserRepository userRepository, WebAuthnRecordRepository webAuthnRecordRepository) {
        this.userRepository = userRepository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    @Override
    public void delete(Bytes credentialId) {
        webAuthnRecordRepository.findByCredentialId(credentialId.getBytes()).ifPresent(entity -> {
            webAuthnRecordRepository.delete(entity);
        });
    }

    @Override
    public void save(CredentialRecord credentialRecord) {
        if (credentialRecord.getCreated().equals(credentialRecord.getLastUsed())) {
            // This is saving new credential

            WebAuthnRecord record = WebAuthnRecord.of(credentialRecord);

            // find the current User via SecurityContext (one most be logged in to save new credential)
            SecurityContext context = SecurityContextHolder.getContext();
            org.springframework.security.core.userdetails.User d = (org.springframework.security.core.userdetails.User) Objects.requireNonNull(context.getAuthentication()).getPrincipal();
            // this is kind of redundant to userEntityId, but I'm a lazy JPA developer...
            record.user = userRepository.findByUsername(Objects.requireNonNull(d).getUsername()).orElse(null);
            webAuthnRecordRepository.save(record);
        } else {
            // update while authenticating, find via userEntityUserId
            WebAuthnRecord authnRecord = webAuthnRecordRepository.findByCredentialId(credentialRecord.getCredentialId().getBytes()).get();
            authnRecord.updateJson(credentialRecord);
            webAuthnRecordRepository.save(authnRecord);
        }
    }

    @Override
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        // This is used when authenticating
        Optional<WebAuthnRecord> byCredentialId = webAuthnRecordRepository.findByCredentialId(credentialId.getBytes());
        return byCredentialId.map(WebAuthnRecord::asCredentialRecord).orElse(null);
    }

    @Override
    @NonNull
    public List<CredentialRecord> findByUserId(Bytes userId) {
        List<WebAuthnRecord> byUserEntityUserId = webAuthnRecordRepository.findByUserEntityUserId(userId.getBytes());
        return byUserEntityUserId.stream().map(WebAuthnRecord::asCredentialRecord).toList();
    }
}
