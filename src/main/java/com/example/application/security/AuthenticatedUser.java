package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import com.example.application.services.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The signed-in user, for the Vaadin UI.
 * <p>
 * Quarkus resolves the user ({@link SecurityIdentity}) for each HTTP request,
 * from the login cookie. A Vaadin UI may also be modified without an HTTP
 * request: in a background thread, with {@code UI.access()} and push. There is
 * no request scope then, and using the {@link SecurityIdentity} fails. So this
 * class remembers the username in the {@link VaadinSession}, and uses that when
 * there is no request. Spring Security integration of Vaadin does the same
 * (VaadinAwareSecurityContextHolderStrategy).
 */
@ApplicationScoped
public class AuthenticatedUser {

    private static final String USERNAME = AuthenticatedUser.class.getName() + ".username";

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
        return getUsername().flatMap(userService::findByUsername);
    }

    /**
     * @return the username of the signed-in user. Works also in
     * {@code UI.access()} from a background thread, but not in a background
     * thread outside of it, as then it is not known which session (user) the
     * thread works for.
     */
    public Optional<String> getUsername() {
        VaadinSession session = VaadinSession.getCurrent();
        if (Arc.container().requestContext().isActive()) {
            String username = identity.isAnonymous() ? null : identity.getPrincipal().getName();
            if (session != null && session.hasLock()) {
                // For later use without a request, see the class Javadoc
                session.setAttribute(USERNAME, username);
            }
            return Optional.ofNullable(username);
        }
        return Optional.ofNullable(session).map(s -> (String) s.getAttribute(USERNAME));
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
        return webAuthnRecordRepository.findByUsername(getUsername().orElseThrow());
    }

    /**
     * Registers a new passkey for the current user.
     *
     * @return a {@link CompletableFuture} that completes when the passkey is stored.
     */
    public CompletableFuture<Void> registerPasskey() {
        return WebAuthnJs.register(getUsername().orElseThrow());
    }

    @Transactional
    public void deletePasskey(WebAuthnRecord passkey) {
        // Only the own passkeys of the user
        webAuthnRecordRepository.delete("id = ?1 and user.username = ?2",
                passkey.getId(), getUsername().orElseThrow());
    }
}
