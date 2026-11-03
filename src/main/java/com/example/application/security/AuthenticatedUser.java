package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository,
                             WebAuthnRecordRepository webAuthnRecordRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    @Transactional
    public Optional<User> get() {
        // The principal type depends on the login method (UserDetails with passwords
        // and one-time tokens, PublicKeyCredentialUserEntity with passkeys), but the
        // name is the username in all cases.
        return authenticationContext.getPrincipalName()
                .flatMap(userRepository::findByUsername);
    }

    public void logout() {
        authenticationContext.logout();
    }

    public List<WebAuthnRecord> getPasskeys() {
        return webAuthnRecordRepository.findByUser(get().orElseThrow());
    }

    /**
     * Registers a new passkey for the current user, using the registration
     * endpoints provided by Spring Security.
     *
     * @return a {@link CompletableFuture} that completes when the passkey is stored.
     */
    public CompletableFuture<Void> registerPasskey() {
        return WebAuthnJs.register(LocalDateTime.now().toString());
    }

    public void deletePasskey(WebAuthnRecord passkey) {
        webAuthnRecordRepository.delete(passkey);
    }
}
