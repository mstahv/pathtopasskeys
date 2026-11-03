package com.example.application.services;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(User user, String newPassword) {
        user.setHashedPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }
}
