package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import com.example.application.services.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class AuthenticatedUser {

    private final SecurityIdentity identity;
    private final UserService userService;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public AuthenticatedUser(SecurityIdentity identity, UserService userService,
                             WebAuthnRecordRepository webAuthnRecordRepository) {
        // A proxy to the identity of the current request
        this.identity = identity;
        this.userService = userService;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    public Optional<User> get() {
        if (identity.isAnonymous()) {
            return Optional.empty();
        }
        return userService.findByUsername(identity.getPrincipal().getName());
    }

    /**
     * Quarkus' logout endpoint removes the login cookie and redirects to the
     * front page. The Vaadin session is closed as well.
     */
    public void logout() {
        UI.getCurrent().getPage().setLocation("/q/webauthn/logout");
        VaadinSession.getCurrent().getSession().invalidate();
    }

    @Transactional
    public List<WebAuthnRecord> getPasskeys() {
        return webAuthnRecordRepository.findByUsername(identity.getPrincipal().getName());
    }

    /**
     * Registers a new passkey for the current user.
     *
     * @return a {@link CompletableFuture} that completes when the passkey is stored.
     */
    public CompletableFuture<Void> registerPasskey() {
        return WebAuthnJs.register(identity.getPrincipal().getName());
    }

    @Transactional
    public void deletePasskey(WebAuthnRecord passkey) {
        // Only the own passkeys of the user
        webAuthnRecordRepository.delete("id = ?1 and user.username = ?2",
                passkey.getId(), identity.getPrincipal().getName());
    }
}
