package com.example.application.security;

import com.example.application.services.UserService;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, single use login tokens ("magic links"), the fallback login
 * method when the user has no passkey (yet) on the current device. Quarkus has
 * no built-in support for these, but they are simple: a random string that
 * maps to a username for a few minutes. {@link LoginLinkSender} delivers the
 * link to the user and {@link OneTimeTokenLogin} turns a token into a login.
 * <p>
 * The tokens are kept in memory. This is fine for single node apps, but in a
 * cluster you would need to share them between the nodes, e.g. in the database.
 */
@ApplicationScoped
public class OneTimeTokens {

    private static final Duration VALIDITY = Duration.ofMinutes(5);

    private record Token(String username, Instant expires) {
    }

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Token> tokens = new ConcurrentHashMap<>();
    private final UserService userService;
    private final LoginLinkSender sender;

    public OneTimeTokens(UserService userService, LoginLinkSender sender) {
        this.userService = userService;
        this.sender = sender;
    }

    /**
     * Sends a login link to the user, if there is a user with the given
     * username. Whether there is, is not told to the caller, so that the
     * login page can't be used to find out usernames.
     */
    public void sendLoginLink(String username) {
        userService.findByUsername(username).ifPresent(user ->
                sender.send(user, OneTimeTokenLogin.PATH + "?token=" + generate(user.getUsername())));
    }

    private String generate(String username) {
        tokens.values().removeIf(token -> token.expires().isBefore(Instant.now()));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(value, new Token(username, Instant.now().plus(VALIDITY)));
        return value;
    }

    /**
     * @return the username for a valid token. The token can't be used again.
     */
    public Optional<String> consume(String value) {
        return Optional.ofNullable(value)
                .map(tokens::remove)
                .filter(token -> token.expires().isAfter(Instant.now()))
                .map(Token::username);
    }
}
