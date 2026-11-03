package com.example.application.services;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class UserService {

    private final UserRepository repository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public UserService(UserRepository repository, WebAuthnRecordRepository webAuthnRecordRepository) {
        this.repository = repository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public List<User> findAll() {
        return repository.listAll(Sort.by("username"));
    }

    /**
     * Creates a user without any credentials. The user signs in first with a
     * one-time token and then registers their own passkeys.
     */
    public User create(String username, String name, Set<Role> roles) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (repository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username " + username + " is already taken");
        }
        var user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setRoles(roles);
        repository.persist(user);
        return user;
    }

    /**
     * Deletes the user and their passkeys.
     */
    public void delete(User user) {
        webAuthnRecordRepository.delete("user.id", user.getId());
        repository.deleteById(user.getId());
    }

    /**
     * Stores a new, already verified, passkey for the user.
     */
    public void addPasskey(String username, WebAuthnCredentialRecord credential, String label) {
        User user = repository.findByUsername(username).orElseThrow();
        webAuthnRecordRepository.persist(WebAuthnRecord.of(user, credential, label));
    }

    public long passkeyCount(User user) {
        return webAuthnRecordRepository.count("user.id", user.getId());
    }
}
