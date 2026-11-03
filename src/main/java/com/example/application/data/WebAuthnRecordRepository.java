package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebAuthnRecordRepository extends JpaRepository<WebAuthnRecord, Long> {

    List<WebAuthnRecord> findByUser(User user);

    Optional<WebAuthnRecord> findByCredentialId(byte[] credentialId);

    List<WebAuthnRecord> findByUserEntityUserId(byte[] bytes);
}
