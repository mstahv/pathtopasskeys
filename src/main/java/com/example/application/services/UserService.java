package com.example.application.services;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public UserService(UserRepository repository, WebAuthnRecordRepository webAuthnRecordRepository) {
        this.repository = repository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    public List<User> findAll() {
        return repository.findAll(Sort.by("username"));
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
        return repository.save(user);
    }

    /**
     * Deletes the user and their passkeys.
     */
    public void delete(User user) {
        webAuthnRecordRepository.deleteAll(webAuthnRecordRepository.findByUser(user));
        repository.delete(user);
    }

    public int passkeyCount(User user) {
        return webAuthnRecordRepository.findByUser(user).size();
    }
}
