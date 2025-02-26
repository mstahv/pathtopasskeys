package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WebAuthnRecordRepository extends JpaRepository<WebAuthnRecord, Long>, JpaSpecificationExecutor<User> {

    List<WebAuthnRecord> findByUser(User user);

    Optional<WebAuthnRecord> findByCredentialId(byte[] credentialId);

    List<WebAuthnRecord> findByUserEntityUserId(byte[] bytes);
}
