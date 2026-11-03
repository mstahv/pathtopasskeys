package com.example.application.security;

import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This repository persists credential records (aka passkeys) to the database.
 * Spring Security uses an in-memory implementation unless you provide one, but
 * you will never want to use that as:
 * <ul>
 *     <li>passkeys only work on single node deployments and</li>
 *     <li>passkeys are lost if you restart the server</li>
 * </ul>
 * <p>
 * This example uses JPA, to keep passkeys next to the rest of the domain model
 * (see {@link WebAuthnRecord}). Spring Security also ships a ready-made JDBC
 * implementation, {@link org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository}
 * (with table definitions in {@code org/springframework/security/user-credentials-schema*.sql}).
 * If that fits your architecture, just expose it as a bean and skip this class.
 */
@Service
@Transactional
public class WebAuthnUserCredentialRepositoryImpl implements UserCredentialRepository {

    private final UserRepository userRepository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public WebAuthnUserCredentialRepositoryImpl(UserRepository userRepository, WebAuthnRecordRepository webAuthnRecordRepository) {
        this.userRepository = userRepository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    @Override
    public void delete(Bytes credentialId) {
        webAuthnRecordRepository.findByCredentialId(credentialId.getBytes())
                .ifPresent(webAuthnRecordRepository::delete);
    }

    /**
     * Called both when a new passkey is registered and when an existing one is
     * used to log in (signature count and last used timestamp change).
     */
    @Override
    public void save(CredentialRecord credentialRecord) {
        WebAuthnRecord record = webAuthnRecordRepository
                .findByCredentialId(credentialRecord.getCredentialId().getBytes())
                .orElseGet(() -> {
                    var newRecord = new WebAuthnRecord();
                    newRecord.user = userRepository
                            .findByWebAuthnId(credentialRecord.getUserEntityUserId().getBytes())
                            .orElseThrow();
                    return newRecord;
                });
        record.update(credentialRecord);
        webAuthnRecordRepository.save(record);
    }

    @Override
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        return webAuthnRecordRepository.findByCredentialId(credentialId.getBytes())
                .map(WebAuthnRecord::asCredentialRecord)
                .orElse(null);
    }

    @Override
    public List<CredentialRecord> findByUserId(Bytes userId) {
        return webAuthnRecordRepository.findByUserEntityUserId(userId.getBytes()).stream()
                .map(WebAuthnRecord::asCredentialRecord)
                .toList();
    }
}
