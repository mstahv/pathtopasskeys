package com.example.application.security;

import com.example.application.services.UserService;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * The endpoints to add a passkey for the signed-in user. The browser side is in
 * {@link WebAuthnJs#register}.
 * <p>
 * Quarkus has a built-in registration endpoint too, but it is meant for signing
 * up: it lets anybody register a passkey for any username that is not taken.
 * Here users already exist, so these endpoints take the username from the
 * current login instead, and are only open for signed-in users (see
 * application.properties). {@link WebAuthnSecurity} does the actual work.
 */
@ApplicationScoped
public class PasskeyRegistration {

    static final String OPTIONS_PATH = "/passkey/register-options";
    static final String REGISTER_PATH = "/passkey/register";

    private static final Logger LOG = Logger.getLogger(PasskeyRegistration.class);

    private final WebAuthnSecurity security;
    private final UserService userService;

    public PasskeyRegistration(WebAuthnSecurity security, UserService userService) {
        this.security = security;
        this.userService = userService;
    }

    void routes(@Observes Router router) {
        router.get(OPTIONS_PATH).handler(this::options);
        router.post(REGISTER_PATH).handler(BodyHandler.create()).handler(this::register);
    }

    /**
     * Step 1: the options (and a challenge) for navigator.credentials.create() in the browser.
     */
    private void options(RoutingContext ctx) {
        blocking(() -> userService.findByUsername(username(ctx)).orElseThrow())
                .flatMap(user -> security.getRegisterChallenge(user.getUsername(), user.getName(), ctx))
                .map(security::toJsonString)
                .subscribe().with(
                        json -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json),
                        error -> fail(ctx, error));
    }

    /**
     * Step 2: verify the new passkey created by the browser, and store it.
     */
    private void register(RoutingContext ctx) {
        String username = username(ctx);
        String label = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        security.register(username, ctx.body().asJsonObject(), ctx)
                .flatMap(credential -> blocking(() -> {
                    userService.addPasskey(username, credential, label);
                    return credential;
                }))
                .subscribe().with(
                        credential -> ctx.response().setStatusCode(204).end(),
                        error -> fail(ctx, error));
    }

    /**
     * Only signed-in users get here (application.properties), so there is always an identity.
     */
    private static String username(RoutingContext ctx) {
        return ((QuarkusHttpUser) ctx.user()).getSecurityIdentity().getPrincipal().getName();
    }

    /**
     * JPA is blocking, so it can't run on the I/O thread that handles the request.
     */
    private static <T> Uni<T> blocking(Supplier<T> supplier) {
        return Uni.createFrom().item(supplier).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private static void fail(RoutingContext ctx, Throwable error) {
        LOG.debug("Passkey registration failed", error);
        ctx.response().setStatusCode(400).end();
    }
}
