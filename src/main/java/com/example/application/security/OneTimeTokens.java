package com.example.application.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, single use login tokens, the fallback login method when the
 * user has no passkey (yet) on the current device. Quarkus has no built-in
 * support for these, but they are simple: a random string that maps to a
 * username for a few minutes. {@link OneTimeTokenLogin} turns a token into a
 * login.
 * <p>
 * This is fine for single node apps, but in a cluster you would need to share
 * the tokens between the nodes, e.g. in the database.
 */
@ApplicationScoped
public class OneTimeTokens {

    private static final Duration VALIDITY = Duration.ofMinutes(5);

    private record Token(String username, Instant expires) {
    }

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Token> tokens = new ConcurrentHashMap<>();

    public String generate(String username) {
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
